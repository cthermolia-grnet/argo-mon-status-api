package org.grnet.status.enums.resources;

import org.grnet.endpoint.scanner.runtime.ApiResource;
public enum TenantResource implements ApiResource {

    TENANT;
    @Override
    public String resourceName() {
        return "Tenant";
    }
}

