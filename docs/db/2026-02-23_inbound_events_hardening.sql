-- Hardening DDL for microservice_patients (PostgreSQL)
-- Safe to run multiple times.

-- inbound_events indexes for query and operational visibility
CREATE INDEX IF NOT EXISTS idx_inbound_events_event_id
  ON inbound_events (event_id);

CREATE INDEX IF NOT EXISTS idx_inbound_events_status
  ON inbound_events (status);

CREATE INDEX IF NOT EXISTS idx_inbound_events_received_on
  ON inbound_events (received_on);

-- Ensure patients subscription fields exist in already provisioned DBs
ALTER TABLE patients
  ADD COLUMN IF NOT EXISTS subscription_status VARCHAR(50);

ALTER TABLE patients
  ADD COLUMN IF NOT EXISTS subscription_ends_on DATE;
