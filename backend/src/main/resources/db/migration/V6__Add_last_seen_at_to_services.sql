------------------------------------------------------------
-- Add last_seen_at column to services table for auto-discovery
------------------------------------------------------------

ALTER TABLE services ADD COLUMN IF NOT EXISTS last_seen_at timestamptz;

-- Set initial value for existing services
UPDATE services SET last_seen_at = created_at WHERE last_seen_at IS NULL;

-- Make it NOT NULL with default
ALTER TABLE services ALTER COLUMN last_seen_at SET DEFAULT now();
ALTER TABLE services ALTER COLUMN last_seen_at SET NOT NULL;

