-- ------------------------------------------------
-- Version: V1.14
-- Description: Update t_tenant table to add status column
-- ------------------------------------------------
ALTER TABLE t_Tenant
ADD COLUMN node BOOLEAN DEFAULT NULL;