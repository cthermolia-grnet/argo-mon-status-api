package org.grnet.status.api.startup;

import io.quarkus.arc.profile.IfBuildProfile;
import io.quarkus.logging.Log;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.grnet.status.services.TenantService;

@ApplicationScoped
@IfBuildProfile("dev")
public class TenantStartupSync {

    @Inject
    TenantService tenantService;

    void onStart(@Observes StartupEvent event) {

        Log.info("Running tenant startup sync (dev mode)...");

        try {
            tenantService.syncWebApiTenantsToLocalDb();
        } catch (Exception e) {
            Log.error("Failed to sync Web API tenants on startup.", e);
        }
    }
}
