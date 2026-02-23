# Politica de Evolucion de Eventos

## Objetivo
Definir como evolucionan contratos inbound/outbound sin romper consumidores existentes.

## Envelope estandar (obligatorio)
Todos los productores deben publicar:
- `event_id` (UUID)
- `event` (string)
- `schema_version` (integer)
- `correlation_id` (UUID)
- `occurred_on` (ISO-8601)
- `payload` (objeto)

El consumidor `patients` rechaza mensajes sin estos campos.

## Reglas de compatibilidad
- `schema_version` es version del `payload` para un `event` especifico.
- Cambios no disruptivos (additive): se permiten en la misma version.
- Cambios disruptivos (rename/remove/tipo incompatible/campos obligatorios nuevos): requieren nueva version (`v2`, `v3`, ...).
- El consumidor `patients` valida version permitida via `INBOUND_SCHEMA_VERSIONS`.
- Un mensaje con version no soportada se rechaza y queda en estado `FAILED`/rechazado segun flujo.

## Politica backward-compatible recomendada
- Productor nuevo debe soportar publicar al menos la version N y N-1 durante ventana de migracion.
- Consumidor nuevo debe aceptar N y N-1 mientras dure la migracion.
- No reutilizar una version para semantica distinta.

## Versionado de schemas en repositorio
- Inbound: `src/main/resources/contracts/inbound/v{n}/`
- Outbound: `src/main/resources/contracts/outbound/v{n}/`
- No editar schemas historicos; solo agregar nuevas carpetas de version.

## Checklist para introducir `v2`
1. Agregar schemas en `contracts/.../v2`.
2. Ajustar validacion/ruteo para aceptar `v2`.
3. Publicar ADR o changelog del breaking change.
4. Ejecutar pruebas de compatibilidad N/N-1.
