-- Add description and deleted_at to projects table
ALTER TABLE projects 
    ADD COLUMN IF NOT EXISTS description TEXT,
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMPTZ;

-- Create index for soft delete queries
CREATE INDEX IF NOT EXISTS idx_projects_owner_deleted 
    ON projects(owner_user_id, deleted_at) 
    WHERE deleted_at IS NULL;


