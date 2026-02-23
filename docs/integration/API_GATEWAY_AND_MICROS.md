# Integracion con API Gateway y Otros Micros

## Gateway -> Patients (HTTP)
- Gateway expone:
  - `GET/POST /patients` -> `patients:/api/patient`
  - `GET/PUT/DELETE/PATCH /patients/{**catch-all}` -> `patients:/api/patient/{**catch-all}`
- URL interna configurable en gateway con `PATIENTS_URL`.

## Correlation end-to-end
- Header estandar: `X-Correlation-Id`.
- Gateway debe reenviar este header al microservicio destino.
- `patients`:
  - Si llega header, lo reutiliza.
  - Si no llega, genera UUID.
  - Siempre lo devuelve en response.
  - Lo registra en MDC como `correlation_id`.

## Integracion asincrona (RabbitMQ)
- Envelope obligatorio:
  - `event_id`, `event`, `schema_version`, `correlation_id`, `occurred_on`, `payload`
- Inbound queue (patients): `INBOUND_RABBITMQ_QUEUE`
- Routing keys inbound soportadas:
  - `suscripciones.suscripcion-actualizada`
  - `suscripciones.suscripcion-eliminada`
  - `contrato.creado`
  - `contrato.cancelado`
  - `contrato.cancelar`

## Eventos outbound emitidos por patients
- `paciente.paciente-creado`
- `paciente.paciente-actualizado`
- `paciente.paciente-eliminado`
- `paciente.direccion-creada`
- `paciente.direccion-actualizada`
- `paciente.direccion-geocodificada`
- `paciente.suscripcion-actualizada`
- `paciente.suscripcion-eliminada`

## Recomendacion para otros micros
- Consumir por contrato JSON versionado (`contracts/.../vN`), no por inferencia.
- Correlacionar logs/trazas por `correlation_id` y `event_id`.
- Manejar reintentos e idempotencia por `event_id`.
