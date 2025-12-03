------------------------------------------------------------
-- Add missing fields to alert_rules table
------------------------------------------------------------

ALTER TABLE alert_rules
ADD COLUMN IF NOT EXISTS aggregation_stat text,
ADD COLUMN IF NOT EXISTS severity text,
ADD COLUMN IF NOT EXISTS notification_channels jsonb DEFAULT '[]'::jsonb;

-- Update existing rows with defaults
UPDATE alert_rules 
SET aggregation_stat = 'AVG' 
WHERE aggregation_stat IS NULL;

UPDATE alert_rules 
SET severity = 'MEDIUM' 
WHERE severity IS NULL;

UPDATE alert_rules 
SET notification_channels = '[]'::jsonb 
WHERE notification_channels IS NULL;

-- Make fields NOT NULL after setting defaults
ALTER TABLE alert_rules
ALTER COLUMN aggregation_stat SET NOT NULL,
ALTER COLUMN severity SET NOT NULL;

COMMENT ON COLUMN alert_rules.aggregation_stat IS 'Aggregation statistic: AVG, P95, P99, MAX, MIN';
COMMENT ON COLUMN alert_rules.severity IS 'Incident severity: low, medium, high, critical';
COMMENT ON COLUMN alert_rules.notification_channels IS 'Array of notification channels (email addresses, webhook URLs)';

