------------------------------------------------------------
-- LOG METADATA TABLE (PostgreSQL)
-- Stores log metadata for efficient querying and pagination
-- Full log content is stored in OpenSearch
------------------------------------------------------------

CREATE TABLE log_metadata (
    id bigserial PRIMARY KEY,
    project_id uuid NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    service_id uuid NOT NULL REFERENCES services(id) ON DELETE CASCADE,
    log_id varchar(200) NOT NULL,
    timestamp timestamptz NOT NULL,
    ingested_at timestamptz NOT NULL DEFAULT now(),
    level varchar(10),
    created_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX idx_logs_project_time ON log_metadata(project_id, timestamp DESC, ingested_at DESC);
CREATE INDEX idx_logs_service_time ON log_metadata(service_id, timestamp DESC, ingested_at DESC);
CREATE INDEX idx_logs_log_id ON log_metadata(log_id);

COMMENT ON TABLE log_metadata IS 'Metadata for logs stored in OpenSearch. Used for efficient querying and search_after pagination.';
COMMENT ON COLUMN log_metadata.log_id IS 'Unique identifier for the log entry (matches OpenSearch document ID)';
COMMENT ON COLUMN log_metadata.ingested_at IS 'Timestamp when log was ingested (for correct ordering when logs arrive late/out-of-order)';

