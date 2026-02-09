-- ------------------------------------------------
-- Version: V1.13
-- Description: Update t_Status_Page (remove api/secret, add tenant_id)
-- ------------------------------------------------

ALTER TABLE t_Status_Page
    ADD COLUMN tenant_id VARCHAR(36) NOT NULL;

ALTER TABLE t_Status_Page
    DROP COLUMN IF EXISTS api,
    DROP COLUMN IF EXISTS secret;

ALTER TABLE t_Status_Page
    ADD CONSTRAINT fk_status_page_tenant
    FOREIGN KEY (tenant_id)
    REFERENCES t_Tenant(id)
    ON DELETE CASCADE;

CREATE INDEX IF NOT EXISTS idx_t_page_tenant_id ON t_Status_Page(tenant_id);
