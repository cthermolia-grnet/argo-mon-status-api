-- ------------------------------------------------
-- Version: V1.9
-- Description: Update t_Tenant_Project table
-- ------------------------------------------------
ALTER TABLE tenant_project
DROP CONSTRAINT fk_tenant_project_project;

ALTER TABLE tenant_project
ADD CONSTRAINT fk_tenant_project_project
    FOREIGN KEY (project_id)
    REFERENCES t_Project(id)
    ON DELETE RESTRICT;
