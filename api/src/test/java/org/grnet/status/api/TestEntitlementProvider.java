package org.grnet.status.api;


import jakarta.enterprise.context.ApplicationScoped;
import org.grnet.endpoint.scanner.runtime.entitlements.Entitlement;
import org.grnet.endpoint.scanner.runtime.entitlements.EntitlementProvider;

import java.util.List;
import jakarta.enterprise.inject.Alternative;
import jakarta.annotation.Priority;
@Alternative
@Priority(1)
@ApplicationScoped
public class TestEntitlementProvider implements EntitlementProvider {

    private List<Entitlement> entitlements = List.of();
    private boolean superAdmin = false;

    @Override
    public List<Entitlement> fetchEntitlements() {
        return entitlements;
    }

    @Override
    public boolean isSuperAdmin() {
        return superAdmin;
    }

    // ✅ TEST CONTROL API (important)
    public void setSuperAdmin(boolean superAdmin) {
        this.superAdmin = superAdmin;
    }

    public void setEntitlements(List<Entitlement> entitlements) {
        this.entitlements = entitlements;
    }

    public void reset() {
        this.superAdmin = false;
        this.entitlements = List.of();
    }
}