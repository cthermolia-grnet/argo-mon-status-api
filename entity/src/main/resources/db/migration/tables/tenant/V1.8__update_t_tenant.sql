-- ------------------------------------------------
-- Version: V1.8
-- Description: Create t_Status_Page table for status pages
-- ------------------------------------------------
ALTER TABLE t_Tenant
ADD COLUMN metadata JSONB DEFAULT '{}'::jsonb;
ALTER TABLE t_Tenant
ALTER COLUMN metadata SET NOT NULL;