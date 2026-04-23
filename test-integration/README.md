# Integration tests (Postman / Newman)

Colección Postman que cubre el ciclo completo de la API de pacientes: health, auth, CRUD de pacientes y direcciones.

## Ejecutar localmente

Requisitos: [Newman](https://github.com/postmanlabs/newman) (`npm i -g newman`) y el servicio corriendo en `http://localhost:8080`.

```bash
newman run test-integration/nur-patients.postman_collection.json \
  -e test-integration/nur-patients.postman_environment.json \
  --env-var adminUser=<usuario> --env-var adminPassword=<password>
```

## Ejecución en CI

El workflow `.github/workflows/integration-tests.yml` levanta el servicio con PostgreSQL y ejecuta Newman automáticamente en cada PR a `main`.

## Estructura de la colección

1. **Health** — `GET /actuator/health`
2. **Auth** — obtiene `access_token` desde Keycloak (grant `password`)
3. **Patients** — `POST` / `GET by id` / `GET list` / `PUT`
4. **Addresses** — `POST` / `GET list` / `DELETE`
5. **Cleanup** — elimina el paciente creado

Cada request incluye asserts (`pm.test`) sobre el status y el payload, y encadena IDs vía `collectionVariables`.
