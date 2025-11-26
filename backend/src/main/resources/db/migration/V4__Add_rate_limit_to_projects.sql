------------------------------------------------------------
-- Add rate_limit_per_minute to projects table
------------------------------------------------------------

ALTER TABLE projects
ADD COLUMN rate_limit_per_minute integer NOT NULL DEFAULT 1000;

COMMENT ON COLUMN projects.rate_limit_per_minute IS 'Rate limit per minute for API key authentication (configurable per project)';

