# microservice_patients

Microservicio de pacientes con DDD + CQRS usando Spring Boot, JPA y Pipelinr.
Incluye Outbox + RabbitMQ para publicar eventos de dominio de forma atomica e inbox para reaccionar a eventos de suscripciones/contratos.

## Requisitos
- Java 17+
- PostgreSQL
- RabbitMQ
- Docker (opcional)

## Configuracion
Variables de entorno:
- `DB_URL` (por defecto `jdbc:postgresql://localhost:5432/nurtricenter_patient_db`)
- `DB_USER` (por defecto `postgres`)
- `DB_PASSWORD` (por defecto `admin`)

RabbitMQ / Outbox:
- `EVENTBUS_DRIVER` (default `rabbitmq`)
- `RABBITMQ_HOST`
- `RABBITMQ_PORT`
- `RABBITMQ_USER`
- `RABBITMQ_PASSWORD`
- `RABBITMQ_VHOST`
- `RABBITMQ_EXCHANGE`
- `RABBITMQ_EXCHANGE_TYPE`
- `RABBITMQ_EXCHANGE_DURABLE`
- `RABBITMQ_ROUTING_KEY`
- `RABBITMQ_QUEUE`
- `RABBITMQ_QUEUE_DURABLE`
- `RABBITMQ_QUEUE_EXCLUSIVE`
- `RABBITMQ_QUEUE_AUTO_DELETE`
- `RABBITMQ_BINDING_KEY`
- `RABBITMQ_PUBLISH_RETRIES`
- `RABBITMQ_PUBLISH_BACKOFF_MS`
- `RABBITMQ_CONNECT_TIMEOUT`
- `RABBITMQ_READ_WRITE_TIMEOUT`
- `RABBITMQ_OUTBOX_BATCH_SIZE`
- `RABBITMQ_OUTBOX_POLL_INTERVAL_MS`

RabbitMQ / Inbound (suscripciones y contratos):
- `INBOUND_RABBITMQ_ENABLED`
- `INBOUND_RABBITMQ_EXCHANGE`
- `INBOUND_RABBITMQ_EXCHANGE_TYPE`
- `INBOUND_RABBITMQ_EXCHANGE_DURABLE`
- `INBOUND_RABBITMQ_QUEUE`
- `INBOUND_RABBITMQ_QUEUE_DURABLE`
- `INBOUND_RABBITMQ_QUEUE_EXCLUSIVE`
- `INBOUND_RABBITMQ_QUEUE_AUTO_DELETE`
- `INBOUND_RABBITMQ_DECLARE_TOPOLOGY` (default `false` para broker externo)
- `INBOUND_RABBITMQ_FAIL_FAST_ON_MISSING_QUEUE` (default `false`)
- `INBOUND_SCHEMA_VERSIONS`
- `INBOUND_RABBITMQ_ROUTING_KEYS`

Se incluye un `.env` con valores para pacientes.

## Endpoints
- `POST /api/patient`
- `GET /api/patient/{id}`
- `GET /api/patient`
- `PUT /api/patient/{id}`
- `DELETE /api/patient/{id}`

Direcciones:
- `POST /api/patient/{id}/address`
- `GET /api/patient/{id}/address`
- `PUT /api/patient/{id}/address/{addressId}`
- `PUT /api/patient/{id}/address/{addressId}/geo`
- `DELETE /api/patient/{id}/address/{addressId}`

## Ejemplos
Crear paciente:
```json
{
  "name": "Juan",
  "lastname": "Perez",
  "birthDate": "15-02-1990",
  "email": "juan.perez@example.com",
  "cellphone": "77778888",
  "document": "1234567",
  "subscriptionId": "11111111-1111-1111-1111-111111111111"
}
```

Agregar direccion:
```json
{
  "label": "Casa",
  "line1": "Av. Principal 123",
  "line2": "Apto 4B",
  "country": "BO",
  "province": "Andres Ibanez",
  "city": "Santa Cruz",
  "latitude": -17.78,
  "longitude": -63.18
}
```

Actualizar direccion:
```json
{
  "label": "Trabajo",
  "line1": "Calle 1",
  "line2": "Oficina 203",
  "country": "BO",
  "province": "Andres Ibanez",
  "city": "Santa Cruz",
  "latitude": -17.78,
  "longitude": -63.18
}
```

Geocodificar direccion:
```json
{
  "latitude": -17.78,
  "longitude": -63.18
}
```

## Eventos publicados (Outbox)
- `paciente.paciente-creado`
- `paciente.paciente-actualizado`
- `paciente.paciente-eliminado`
- `paciente.direccion-creada`
- `paciente.direccion-actualizada`
- `paciente.direccion-geocodificada`
- `paciente.suscripcion-actualizada`
- `paciente.suscripcion-eliminada`

## Eventos consumidos (Inbound)
- `suscripciones.suscripcion-actualizada`
- `suscripciones.suscripcion-eliminada`
- `contrato.creado`
- `contrato.cancelado`
- `contrato.cancelar`

El consumidor inbound usa inbox (`inbound_events`) para idempotencia por `event_id`.
El envelope es obligatorio y se rechaza si faltan campos:
`event_id`, `event`, `schema_version`, `correlation_id`, `occurred_on`, `payload`.

## Observabilidad
- Metricas: `GET /actuator/metrics`
- Metricas inbound:
  - `patients.inbound.events.received`
  - `patients.inbound.events.processed`
  - `patients.inbound.events.failed`
  - `patients.inbound.events.duplicated`
  - `patients.inbound.handler.latency`
- Logs con correlacion via MDC (`correlation_id`) en listener y handlers.
- Correlacion HTTP via header `X-Correlation-Id` (se acepta/genera y se retorna en response).

## Gobierno de eventos y reglas de dominio
- Politica de evolucion y compatibilidad: `docs/events/EVOLUTION_POLICY.md`
- Reglas de negocio de suscripcion: `docs/domain/SUBSCRIPTION_BUSINESS_RULES.md`
- Integracion API Gateway y otros micros: `docs/integration/API_GATEWAY_AND_MICROS.md`

## DDL de endurecimiento
Para ambientes ya creados, aplicar:

```bash
psql -U postgres -h localhost -d nurtricenter_patient_db -f docs/db/2026-02-23_inbound_events_hardening.sql
```

## Docker
Levantar todo (API + Postgres; RabbitMQ es externo):
```bash
docker compose up --build
```

Servicios:
- API: `http://localhost:8080`
- Postgres: `localhost:5432`
- RabbitMQ: externo, configurado por `.env` (`RABBITMQ_HOST`, `RABBITMQ_PORT`, etc.)

Bajar:
```bash
docker compose down
```

Datos persistentes en volumen `pgdata`.

## Smoke E2E de eventos críticos
Con el stack levantado (`docker compose up -d --build`):

1) Provisionar RabbitMQ externo (idempotente):
```bash
./scripts/e2e/provision_inbound_rabbit.sh
```

2) Ejecutar smoke:
```bash
./scripts/e2e/inbound_critical_events_smoke.sh
```

Este script publica:
- `suscripciones.suscripcion-actualizada`
- `suscripciones.suscripcion-eliminada`
- `contrato.creado`
- `contrato.cancelar`
- `contrato.cancelado`
- un `contrato.cancelar` invalido (sin `contratoId`) para validar `FAILED`

Y valida en DB:
- resumen por estado en `inbound_events`
- ultimos eventos (`PROCESSED/FAILED`)

Variables utiles para RabbitMQ externo en el script:
- `RABBIT_MGMT_HOST` (default `154.38.180.80`)
- `RABBIT_MGMT_PORT` (default `15672`)
- `RABBIT_USER`
- `RABBIT_PASS`

Si ves `NOT_FOUND - no queue 'pacientes.inbound'`, ejecuta primero `provision_inbound_rabbit.sh`.
Si ves `PRECONDITION_FAILED ... exchange 'outbox.events' ... received 'topic' but current is 'fanout'`,
configura `INBOUND_RABBITMQ_EXCHANGE_TYPE=fanout` (default recomendado).
Si la cola aun no existe, el listener inbound queda deshabilitado automaticamente en arranque
hasta que la cola sea aprovisionada y se reinicie el servicio.
Para entornos externos, el default recomendado es no cortar arranque si la cola aun no existe:
`INBOUND_RABBITMQ_FAIL_FAST_ON_MISSING_QUEUE=false`.
Si deseas modo estricto, habilita:
`INBOUND_RABBITMQ_FAIL_FAST_ON_MISSING_QUEUE=true`.


psql -U postgres -h localhost -p 5432
\c nurtricenter_patient_db
SELECT * FROM outbox_events;
