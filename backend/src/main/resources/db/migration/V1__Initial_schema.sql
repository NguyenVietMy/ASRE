------------------------------------------------------------
-- Extensions
------------------------------------------------------------

-- UUID generator
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- TimescaleDB
CREATE EXTENSION IF NOT EXISTS timescaledb;

-- pgvector
CREATE EXTENSION IF NOT EXISTS vector;

------------------------------------------------------------
-- ENUMS
------------------------------------------------------------

CREATE TYPE user_role AS ENUM ('admin', 'member');
CREATE TYPE project_member_role AS ENUM ('owner', 'editor', 'viewer');
CREATE TYPE incident_status AS ENUM ('open', 'investigating', 'resolved');
CREATE TYPE incident_severity AS ENUM ('low', 'medium', 'high', 'critical');
CREATE TYPE incident_event_type AS ENUM ('metric_spike', 'new_log_pattern', 'comment', 'ai');
CREATE TYPE kb_source_type AS ENUM ('runbook', 'incident_note', 'historical_incident');

------------------------------------------------------------
-- USERS & PROJECTS
------------------------------------------------------------

CREATE TABLE users (
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    email text UNIQUE NOT NULL,
    password_hash text NOT NULL,
    role user_role NOT NULL DEFAULT 'member',
    created_at timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE projects (
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    name text NOT NULL,
    api_key text UNIQUE NOT NULL,
    owner_user_id uuid NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT fk_project_owner FOREIGN KEY (owner_user_id) REFERENCES users(id)
);

CREATE TABLE project_members (
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    project_id uuid NOT NULL,
    user_id uuid NOT NULL,
    role project_member_role NOT NULL DEFAULT 'viewer',
    CONSTRAINT fk_pm_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT fk_pm_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

------------------------------------------------------------
-- SERVICES
------------------------------------------------------------

CREATE TABLE services (
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    project_id uuid NOT NULL,
    name text NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT fk_service_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
);


------------------------------------------------------------
-- METRICS (TimescaleDB hypertable)
------------------------------------------------------------

CREATE TABLE metrics (
    id bigserial PRIMARY KEY,
    time timestamptz NOT NULL,
    service_id uuid NOT NULL,
    metric_name text NOT NULL,
    value double precision NOT NULL,
    tags jsonb,
    CONSTRAINT fk_metrics_service FOREIGN KEY (service_id) REFERENCES services(id) ON DELETE CASCADE
);

-- Convert into hypertable
SELECT create_hypertable('metrics', 'time', if_not_exists => TRUE);

-- Indexes for fast queries
CREATE INDEX idx_metrics_lookup
    ON metrics(service_id, metric_name, time DESC);

CREATE INDEX idx_metrics_tags
    ON metrics USING GIN (tags);


------------------------------------------------------------
-- LOGS (OpenSearch pointer table)
------------------------------------------------------------

CREATE TABLE log_entries (
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    service_id uuid NOT NULL,
    timestamp timestamptz NOT NULL,
    level text NOT NULL,
    message text,
    opensearch_doc_id text,
    trace_id text,
    CONSTRAINT fk_logs_service FOREIGN KEY (service_id) REFERENCES services(id) ON DELETE CASCADE
);

CREATE INDEX idx_logs_service_time ON log_entries(service_id, timestamp DESC);
CREATE INDEX idx_logs_trace ON log_entries(trace_id);


------------------------------------------------------------
-- ALERT RULES
------------------------------------------------------------

CREATE TABLE alert_rules (
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    project_id uuid NOT NULL,
    service_id uuid NOT NULL,
    name text NOT NULL,
    metric_name text NOT NULL,
    operator text NOT NULL, -- >, <, >=, <=
    threshold double precision NOT NULL,
    window_minutes integer NOT NULL,
    duration_minutes integer NOT NULL,
    enabled boolean NOT NULL DEFAULT TRUE,
    created_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT fk_alert_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT fk_alert_service FOREIGN KEY (service_id) REFERENCES services(id) ON DELETE CASCADE
);

CREATE INDEX idx_alert_rules_project ON alert_rules(project_id);


------------------------------------------------------------
-- INCIDENTS & EVENTS
------------------------------------------------------------

CREATE TABLE incidents (
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    project_id uuid NOT NULL,
    service_id uuid NOT NULL,
    rule_id uuid,
    status incident_status NOT NULL DEFAULT 'open',
    started_at timestamptz NOT NULL DEFAULT now(),
    resolved_at timestamptz,
    summary text,
    ai_summary text,
    severity incident_severity NOT NULL DEFAULT 'medium',

    CONSTRAINT fk_incident_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    CONSTRAINT fk_incident_service FOREIGN KEY (service_id) REFERENCES services(id) ON DELETE CASCADE,
    CONSTRAINT fk_incident_rule FOREIGN KEY (rule_id) REFERENCES alert_rules(id)
);

CREATE INDEX idx_incidents_project ON incidents(project_id);
CREATE INDEX idx_incidents_service ON incidents(service_id);

CREATE TABLE incident_events (
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    incident_id uuid NOT NULL,
    event_type incident_event_type NOT NULL,
    content jsonb,
    timestamp timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT fk_incident_event FOREIGN KEY (incident_id) REFERENCES incidents(id) ON DELETE CASCADE
);

CREATE INDEX idx_incident_events_incident ON incident_events(incident_id);


------------------------------------------------------------
-- RUNBOOKS
------------------------------------------------------------

CREATE TABLE runbooks (
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    project_id uuid NOT NULL,
    name text NOT NULL,
    s3_path text NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT fk_runbook_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
);

CREATE INDEX idx_runbooks_project ON runbooks(project_id);


------------------------------------------------------------
-- INCIDENT NOTES
------------------------------------------------------------

CREATE TABLE incident_notes (
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    incident_id uuid NOT NULL,
    author_user_id uuid NOT NULL,
    content text NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT fk_notes_incident FOREIGN KEY (incident_id) REFERENCES incidents(id) ON DELETE CASCADE,
    CONSTRAINT fk_notes_user FOREIGN KEY (author_user_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX idx_notes_incident ON incident_notes(incident_id);


------------------------------------------------------------
-- RAG VECTOR TABLE
------------------------------------------------------------

CREATE TABLE kb_embeddings (
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    project_id uuid NOT NULL,
    source_type kb_source_type NOT NULL,
    source_id uuid NOT NULL,
    chunk_text text NOT NULL,
    embedding vector(1536) NOT NULL,
    created_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT fk_kb_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE
);

CREATE INDEX idx_kb_project ON kb_embeddings(project_id);
CREATE INDEX idx_kb_vector ON kb_embeddings USING ivfflat (embedding vector_cosine_ops);


------------------------------------------------------------
-- AI RUN HISTORY
------------------------------------------------------------

CREATE TABLE ai_runs (
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    incident_id uuid NOT NULL,
    input_context jsonb NOT NULL,
    output_text text,
    model_used text,
    latency_ms int,
    created_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT fk_ai_incident FOREIGN KEY (incident_id) REFERENCES incidents(id) ON DELETE CASCADE
);

CREATE INDEX idx_ai_incident ON ai_runs(incident_id);


------------------------------------------------------------
-- ANOMALY DETECTION
------------------------------------------------------------

CREATE TABLE anomaly_detection_results (
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    service_id uuid NOT NULL,
    metric_name text NOT NULL,
    timestamp timestamptz NOT NULL,
    z_score double precision NOT NULL,
    is_anomaly boolean NOT NULL,
    context jsonb,
    CONSTRAINT fk_anom_service FOREIGN KEY (service_id) REFERENCES services(id) ON DELETE CASCADE
);

CREATE INDEX idx_anom_lookup
    ON anomaly_detection_results(service_id, metric_name, timestamp DESC);


------------------------------------------------------------
-- PLATFORM INTERNAL METADATA
------------------------------------------------------------

CREATE TABLE rate_limit_buckets (
    api_key text PRIMARY KEY,
    window_start timestamptz NOT NULL,
    count int NOT NULL
);

CREATE TABLE ingestion_failures (
    id uuid PRIMARY KEY DEFAULT uuid_generate_v4(),
    payload jsonb NOT NULL,
    error text,
    created_at timestamptz NOT NULL DEFAULT now()
);
