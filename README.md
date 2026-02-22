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

## Docker
Levantar todo (API + Postgres + RabbitMQ):
```bash
docker compose up --build
```

Servicios:
- API: `http://localhost:8080`
- Postgres: `localhost:5432`
- RabbitMQ: `localhost:5672`
- RabbitMQ UI: `http://localhost:15672` (admin / rabbit_mq)

Bajar:
```bash
docker compose down
```

Datos persistentes en volumen `pgdata`.


psql -U postgres -h localhost -p 5432
\c nurtricenter_patient_db
SELECT * FROM outbox_events;
