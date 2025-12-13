------------------------------------------------------------
-- Add project_id column to metrics table for multi-tenant isolation
------------------------------------------------------------

ALTER TABLE metrics ADD COLUMN project_id uuid NOT NULL;

-- Create indexes that include project_id for multi-tenant isolation
CREATE INDEX idx_metrics_project_time
    ON metrics(project_id, time DESC);

CREATE INDEX idx_metrics_project_service
    ON metrics(project_id, service_id, time DESC);

CREATE INDEX idx_metrics_project_name
    ON metrics(project_id, metric_name, time DESC);

