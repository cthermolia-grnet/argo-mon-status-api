-- ------------------------------------------------
-- Version: V1.12
-- Description: Create t_Tenant_Invitation table for tenant invitations
-- ------------------------------------------------
CREATE TABLE t_Tenant_Invitation (
    id VARCHAR(36),
    tenant_id VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    role VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    responded_at TIMESTAMP,
    created_by VARCHAR(255),
    responded_by VARCHAR(255),
    PRIMARY KEY (id),
    FOREIGN KEY (tenant_id) REFERENCES t_Tenant(id) ON DELETE CASCADE
);

CREATE INDEX idx_tenant_invitation_tenant
    ON t_Tenant_Invitation(tenant_id);

CREATE INDEX idx_tenant_invitation_pending
    ON t_Tenant_Invitation(tenant_id, email)
    WHERE status = 'PENDING';