#!/usr/bin/env bash
set -euo pipefail

RABBIT_MGMT_HOST="${RABBIT_MGMT_HOST:-154.38.180.80}"
RABBIT_MGMT_PORT="${RABBIT_MGMT_PORT:-15672}"
RABBIT_USER="${RABBIT_USER:-admin}"
RABBIT_PASS="${RABBIT_PASS:-rabbit_mq}"
RABBIT_VHOST="${RABBIT_VHOST:-/}"

EXCHANGE="${INBOUND_RABBITMQ_EXCHANGE:-outbox.events}"
EXCHANGE_TYPE="${INBOUND_RABBITMQ_EXCHANGE_TYPE:-fanout}"
QUEUE="${INBOUND_RABBITMQ_QUEUE:-pacientes.inbound}"
ROUTING_KEYS="${INBOUND_RABBITMQ_ROUTING_KEYS:-suscripciones.suscripcion-actualizada,suscripciones.suscripcion-eliminada,contrato.creado,contrato.cancelado,contrato.cancelar}"

BASE_URL="http://${RABBIT_MGMT_HOST}:${RABBIT_MGMT_PORT}/api"
VHOST_ENCODED="%2F"
if [[ "${RABBIT_VHOST}" != "/" ]]; then
  VHOST_ENCODED="${RABBIT_VHOST}"
fi

api_put() {
  local path="$1"
  local json="$2"
  curl -fsS -u "${RABBIT_USER}:${RABBIT_PASS}" \
    -H "content-type: application/json" \
    -X PUT "${BASE_URL}${path}" \
    -d "${json}" >/dev/null
}

echo "Provisioning inbound exchange/queue on RabbitMQ ${RABBIT_MGMT_HOST}:${RABBIT_MGMT_PORT}"

api_put "/exchanges/${VHOST_ENCODED}/${EXCHANGE}" \
  "{\"type\":\"${EXCHANGE_TYPE}\",\"durable\":true,\"auto_delete\":false,\"internal\":false,\"arguments\":{}}"

api_put "/queues/${VHOST_ENCODED}/${QUEUE}" \
  '{"durable":true,"auto_delete":false,"arguments":{}}'

if [[ "${EXCHANGE_TYPE}" == "fanout" ]]; then
  api_put "/bindings/${VHOST_ENCODED}/e/${EXCHANGE}/q/${QUEUE}" \
    '{"routing_key":"","arguments":{}}'
  echo "Bound ${QUEUE} <- ${EXCHANGE} (fanout)"
else
  IFS=',' read -r -a KEYS <<< "${ROUTING_KEYS}"
  for key in "${KEYS[@]}"; do
    rk="$(echo "${key}" | xargs)"
    if [[ -z "${rk}" ]]; then
      continue
    fi
    api_put "/bindings/${VHOST_ENCODED}/e/${EXCHANGE}/q/${QUEUE}" \
      "{\"routing_key\":\"${rk}\",\"arguments\":{}}"
    echo "Bound ${QUEUE} <- ${EXCHANGE} (${rk})"
  done
fi

echo "Provisioning completed."
