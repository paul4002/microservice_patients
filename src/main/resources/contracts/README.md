# Event Contracts

This folder freezes message contracts by version.

Rules:
- Do not modify existing schemas in place.
- Add new versions under `v2`, `v3`, etc. when contract changes.
- Producers must send the envelope fields:
  - `event_id`
  - `event`
  - `schema_version`
  - `correlation_id`
  - `occurred_on`
  - `payload`

Current stable version: `v1`.
