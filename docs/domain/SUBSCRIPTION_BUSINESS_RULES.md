# Reglas de Negocio de Suscripcion

## Estados soportados
- `ACTIVE`
- `EXPIRED`
- `CANCELLED`

## Reglas de transicion en `patients`
- `contrato.creado`:
  - Si `pacienteId` existe, se sincroniza suscripcion del paciente con `contratoId`.
  - Estado se resuelve desde `payload.estado/status` o por fecha fin.
- `suscripciones.suscripcion-actualizada`:
  - Busca pacientes por `suscripcionId`.
  - Actualiza estado y fecha fin si hay cambios reales.
- `suscripciones.suscripcion-eliminada`, `contrato.cancelar`, `contrato.cancelado`:
  - Busca pacientes por `suscripcionId/contratoId`.
  - Elimina vinculo de suscripcion y marca `CANCELLED`.

## Regla de vencimiento
- Si `fechaFin` < fecha actual UTC del servicio, el estado final se fuerza a `EXPIRED`.
- Si no viene estado en payload y no esta vencida, se considera `ACTIVE`.

## Reglas de idempotencia
- `event_id` es unico por evento inbound.
- Si llega dos veces el mismo `event_id`, solo se procesa una vez.
- Duplicados se marcan como ignorados sin reejecutar side-effects.

## Reglas de validacion
- IDs de dominio (`pacienteId`, `suscripcionId`, `contratoId`) deben ser UUID validos.
- `schema_version` debe estar dentro de `INBOUND_SCHEMA_VERSIONS`.
