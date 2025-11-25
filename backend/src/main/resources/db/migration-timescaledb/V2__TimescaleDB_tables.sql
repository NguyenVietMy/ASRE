------------------------------------------------------------
-- EXTENSIONS
------------------------------------------------------------

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS timescaledb;

------------------------------------------------------------
-- METRICS (hypertable)
-- Stores RAW SAMPLES only (not pre-aggregated percentiles)
-- SDK sends: metric_name="latency_ms", value=123.5
-- Backend computes p50/p95/p99 via percentile_cont() at query time
------------------------------------------------------------

CREATE TABLE metrics (
    id bigserial,
    time timestamptz NOT NULL,
    project_id uuid NOT NULL,  -- Multi-tenant isolation (mandatory)
    service_id uuid NOT NULL,
    metric_name text NOT NULL,
    value double precision NOT NULL,
    tags jsonb,
    PRIMARY KEY (time, id)
);

SELECT create_hypertable('metrics', 'time', if_not_exists => TRUE);

-- Multi-tenant indexes (project_id first for isolation)
CREATE INDEX idx_metrics_project_time
    ON metrics(project_id, time DESC);

CREATE INDEX idx_metrics_project_service
    ON metrics(project_id, service_id, time DESC);

CREATE INDEX idx_metrics_project_name
    ON metrics(project_id, metric_name, time DESC);

CREATE INDEX idx_metrics_tags
    ON metrics USING GIN (tags);

------------------------------------------------------------
-- LOG ENTRIES (if storing partial logs in TSDB)
-- ingested_at ensures correct ordering when logs arrive late/out-of-order
------------------------------------------------------------

CREATE TABLE log_entries (
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    project_id uuid NOT NULL,  -- Multi-tenant isolation
    service_id uuid NOT NULL,
    timestamp timestamptz NOT NULL,
    ingested_at timestamptz NOT NULL DEFAULT now(),  -- For correct ordering
    level text NOT NULL,
    message text,
    opensearch_doc_id text,
    trace_id text
);

-- Index includes ingested_at for proper sorting (timestamp + ingested_at)
CREATE INDEX idx_logs_project_time ON log_entries(project_id, timestamp DESC, ingested_at DESC);
CREATE INDEX idx_logs_service_time ON log_entries(service_id, timestamp DESC, ingested_at DESC);
CREATE INDEX idx_logs_trace ON log_entries(trace_id);

------------------------------------------------------------
-- ANOMALY DETECTION RESULTS (time-series)
------------------------------------------------------------

CREATE TABLE anomaly_detection_results (
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    project_id uuid NOT NULL,  -- Multi-tenant isolation
    service_id uuid NOT NULL,
    metric_name text NOT NULL,
    timestamp timestamptz NOT NULL,
    z_score double precision NOT NULL,
    is_anomaly boolean NOT NULL,
    context jsonb
);

-- Multi-tenant index (project_id first for isolation)
CREATE INDEX idx_anom_project_lookup
    ON anomaly_detection_results(project_id, service_id, metric_name, timestamp DESC);
