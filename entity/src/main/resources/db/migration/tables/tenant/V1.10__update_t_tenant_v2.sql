-- ------------------------------------------------
-- Version: V1.10
-- Description: Update t_tenant table to add status column
-- ------------------------------------------------
ALTER TABLE t_Tenant
ADD COLUMN status JSONB DEFAULT '{}'::jsonb;
ALTER TABLE t_Tenant
ALTER COLUMN status SET NOT NULL;