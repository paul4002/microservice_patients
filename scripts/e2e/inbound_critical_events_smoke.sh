#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
RABBIT_MGMT_HOST="${RABBIT_MGMT_HOST:-154.38.180.80}"
RABBIT_MGMT_PORT="${RABBIT_MGMT_PORT:-15672}"
RABBIT_API="${RABBIT_API:-http://${RABBIT_MGMT_HOST}:${RABBIT_MGMT_PORT}/api/exchanges/%2F/outbox.events/publish}"
RABBIT_HEALTH_API="${RABBIT_HEALTH_API:-http://${RABBIT_MGMT_HOST}:${RABBIT_MGMT_PORT}/api/health/checks/alarms}"
RABBIT_USER="${RABBIT_USER:-admin}"
RABBIT_PASS="${RABBIT_PASS:-rabbit_mq}"
DB_CONTAINER="${DB_CONTAINER:-nurtricenter_patient_db}"
DB_NAME="${DB_NAME:-nurtricenter_patient_db}"
DB_USER="${DB_USER:-postgres}"
PUBLISHED_EVENT_IDS=()

uuid() {
  cat /proc/sys/kernel/random/uuid
}

publish_event() {
  local event_name="$1"
  local routing_key="$2"
  local payload_json="$3"
  local event_id
  local correlation_id
  local occurred_on

  event_id="$(uuid)"
  correlation_id="$(uuid)"
  occurred_on="$(date -u +"%Y-%m-%dT%H:%M:%SZ")"

  local envelope
  envelope=$(
    cat <<JSON
{"event_id":"${event_id}","event":"${event_name}","schema_version":1,"correlation_id":"${correlation_id}","occurred_on":"${occurred_on}","payload":${payload_json}}
JSON
  )

  local escaped_envelope
  escaped_envelope="$(printf '%s' "${envelope}" | sed 's/\\/\\\\/g; s/"/\\"/g')"

  local body
  body=$(
    cat <<JSON
{"properties":{},"routing_key":"${routing_key}","payload":"${escaped_envelope}","payload_encoding":"string"}
JSON
  )

  echo "Publishing ${event_name} (${event_id})"
  PUBLISHED_EVENT_IDS+=("${event_id}")
  local response
  response="$(curl -fsS --retry 5 --retry-delay 1 --retry-connrefused -u "${RABBIT_USER}:${RABBIT_PASS}" \
    -H "content-type: application/json" \
    -X POST "${RABBIT_API}" \
    -d "${body}")"
  if [[ "${response}" != *'"routed":true'* ]]; then
    echo "Publish not routed for ${event_name}: ${response}"
    exit 1
  fi
}

wait_for_rabbitmq() {
  echo "Waiting for RabbitMQ Management API..."
  local attempts=0
  until curl -fsS -u "${RABBIT_USER}:${RABBIT_PASS}" "${RABBIT_HEALTH_API}" >/dev/null; do
    attempts=$((attempts + 1))
    if [[ "${attempts}" -ge 30 ]]; then
      echo "RabbitMQ Management API not ready after 30 attempts."
      exit 1
    fi
    sleep 1
  done
}

main() {
  echo "== Inbound critical events smoke test =="
  echo "Working dir: ${ROOT_DIR}"
  wait_for_rabbitmq

  publish_event \
    "suscripciones.suscripcion-actualizada" \
    "suscripciones.suscripcion-actualizada" \
    '{"suscripcionId":"3fa85f64-5717-4562-b3fc-2c963f66afa6","nombre":"Plan 15 dias"}'

  publish_event \
    "suscripciones.suscripcion-eliminada" \
    "suscripciones.suscripcion-eliminada" \
    '{"suscripcionId":"3fa85f64-5717-4562-b3fc-2c963f66afa6"}'

  publish_event \
    "contrato.creado" \
    "contrato.creado" \
    '{"contratoId":"3fa85f64-5717-4562-b3fc-2c963f66afa6","pacienteId":"f2d06f1a-f6f6-495e-a8a5-b0153f0d13eb","tipoServicio":"asesoramiento","fechaInicio":"2026-02-23","fechaFin":"2026-03-10"}'

  publish_event \
    "contrato.cancelar" \
    "contrato.cancelar" \
    '{"contratoId":"3fa85f64-5717-4562-b3fc-2c963f66afa6","motivoCancelacion":"solicitud-paciente"}'

  publish_event \
    "contrato.cancelado" \
    "contrato.cancelado" \
    '{"contratoId":"3fa85f64-5717-4562-b3fc-2c963f66afa6","motivoCancelacion":"politica-interna"}'

  # Deliberate invalid payload to verify FAILED status persistence.
  publish_event \
    "contrato.cancelar" \
    "contrato.cancelar" \
    '{"motivoCancelacion":"payload-invalido-sin-contratoId"}'

  echo
  echo "Waiting 5 seconds for consumer processing..."
  sleep 5

  echo
  local in_clause
  in_clause="$(printf "'%s'," "${PUBLISHED_EVENT_IDS[@]}")"
  in_clause="${in_clause%,}"

  echo
  echo "Inbound events status summary (this run):"
  docker exec "${DB_CONTAINER}" psql -U "${DB_USER}" -d "${DB_NAME}" -c \
    "SELECT status, count(*) FROM inbound_events WHERE event_id::text IN (${in_clause}) GROUP BY status ORDER BY status;"

  echo
  echo "Inbound rows (this run):"
  docker exec "${DB_CONTAINER}" psql -U "${DB_USER}" -d "${DB_NAME}" -c \
    "SELECT event_id, event_name, status, received_on, processed_on, left(coalesce(last_error,''),120) as last_error FROM inbound_events WHERE event_id::text IN (${in_clause}) ORDER BY received_on DESC;"

  echo
  echo "Expected: all events must be persisted with status PROCESSED or FAILED."
}

main "$@"
