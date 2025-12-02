-- ------------------------------------------------
-- Version: V1.7
-- Description: Create join table for Tenant ↔ Project (many-to-many)
-- ------------------------------------------------

CREATE TABLE tenant_project (
    id          VARCHAR(36) PRIMARY KEY,
    tenant_id   VARCHAR(36)  NOT NULL,
    project_id  VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),

    CONSTRAINT uq_tenant_project UNIQUE (tenant_id, project_id),

    CONSTRAINT fk_tenant_project_tenant
        FOREIGN KEY (tenant_id) REFERENCES t_Tenant(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_tenant_project_project
        FOREIGN KEY (project_id) REFERENCES t_Project(id)
        ON DELETE CASCADE
);