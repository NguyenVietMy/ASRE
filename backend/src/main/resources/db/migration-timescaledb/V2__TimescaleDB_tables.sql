------------------------------------------------------------
-- EXTENSIONS
------------------------------------------------------------

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS timescaledb;

------------------------------------------------------------
-- METRICS (hypertable)
------------------------------------------------------------

CREATE TABLE metrics (
    id bigserial,
    time timestamptz NOT NULL,
    service_id uuid NOT NULL,
    metric_name text NOT NULL,
    value double precision NOT NULL,
    tags jsonb,
    PRIMARY KEY (time, id)
);

SELECT create_hypertable('metrics', 'time', if_not_exists => TRUE);

CREATE INDEX idx_metrics_lookup
    ON metrics(service_id, metric_name, time DESC);

CREATE INDEX idx_metrics_tags
    ON metrics USING GIN (tags);

------------------------------------------------------------
-- LOG ENTRIES (if storing partial logs in TSDB)
------------------------------------------------------------

CREATE TABLE log_entries (
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    service_id uuid NOT NULL,
    timestamp timestamptz NOT NULL,
    level text NOT NULL,
    message text,
    opensearch_doc_id text,
    trace_id text
);

CREATE INDEX idx_logs_service_time ON log_entries(service_id, timestamp DESC);
CREATE INDEX idx_logs_trace ON log_entries(trace_id);

------------------------------------------------------------
-- ANOMALY DETECTION RESULTS (time-series)
------------------------------------------------------------

CREATE TABLE anomaly_detection_results (
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    service_id uuid NOT NULL,
    metric_name text NOT NULL,
    timestamp timestamptz NOT NULL,
    z_score double precision NOT NULL,
    is_anomaly boolean NOT NULL,
    context jsonb
);

CREATE INDEX idx_anom_lookup
    ON anomaly_detection_results(service_id, metric_name, timestamp DESC);
