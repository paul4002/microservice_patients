CREATE TABLE IF NOT EXISTS patients (
    id                       uuid PRIMARY KEY,
    name                     varchar(255),
    lastname                 varchar(255),
    birth_date               date,
    email                    varchar(255) UNIQUE,
    cellphone                varchar(255) UNIQUE,
    document                 varchar(255) UNIQUE,
    subscription_id          uuid,
    subscription_status      varchar(32),
    subscription_ends_on     date
);

CREATE TABLE IF NOT EXISTS addresses (
    id          uuid PRIMARY KEY,
    patient_id  uuid NOT NULL REFERENCES patients(id) ON DELETE CASCADE,
    label       varchar(255),
    line1       varchar(255),
    line2       varchar(255),
    country     varchar(255),
    province    varchar(255),
    city        varchar(255),
    latitude    float8,
    longitude   float8,
    state       boolean
);

CREATE INDEX IF NOT EXISTS idx_addresses_patient_id ON addresses(patient_id);

CREATE TABLE IF NOT EXISTS outbox_events (
    id                uuid PRIMARY KEY,
    event_id          uuid,
    aggregate_type    varchar(255),
    aggregate_id      varchar(255),
    event_type        varchar(255),
    event_name        varchar(255),
    routing_key       varchar(255),
    schema_version    integer,
    correlation_id    uuid,
    payload           text,
    occurred_on       timestamp without time zone,
    processed_on      timestamp without time zone,
    attempts          integer,
    next_attempt_at   timestamp without time zone,
    last_error        text
);

CREATE INDEX IF NOT EXISTS idx_outbox_pending
    ON outbox_events(processed_on, next_attempt_at, occurred_on)
    WHERE processed_on IS NULL;

CREATE TABLE IF NOT EXISTS inbound_events (
    id              uuid PRIMARY KEY,
    event_id        uuid NOT NULL UNIQUE,
    event_name      varchar(255),
    routing_key     varchar(255),
    correlation_id  uuid,
    schema_version  integer,
    status          varchar(32),
    occurred_on     timestamp without time zone,
    received_on     timestamp without time zone,
    processed_on    timestamp without time zone,
    updated_on      timestamp without time zone,
    payload         text,
    last_error      text
);

CREATE INDEX IF NOT EXISTS idx_inbound_events_event_id    ON inbound_events(event_id);
CREATE INDEX IF NOT EXISTS idx_inbound_events_status      ON inbound_events(status);
CREATE INDEX IF NOT EXISTS idx_inbound_events_received_on ON inbound_events(received_on);
