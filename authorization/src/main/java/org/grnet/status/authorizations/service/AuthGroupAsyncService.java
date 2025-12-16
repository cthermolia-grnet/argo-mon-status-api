package org.grnet.status.authorizations.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.grnet.status.authorizations.groups.GroupManagement;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.Map;

@ApplicationScoped
public class AuthGroupAsyncService {

    private static final Logger LOG = Logger.getLogger(AuthGroupAsyncService.class);

    @Inject
    GroupManagement groupManagement;

    @Inject
    ManagedExecutor executor;

    public void createGroup(String parentPath, String name, List<String> roles, Map<String, List<String>> attributes) {

        executor.runAsync(() -> {
            try {
                groupManagement.createGroup(parentPath, name, roles, attributes);
                LOG.infof("Async group created: %s/%s", parentPath, name);
            } catch (Exception e) {
                LOG.errorf("Async group creation failed for %s/%s: %s",
                        parentPath, name, e.getMessage());
            }
        });
    }

    public void deleteGroup(String fullPath) {

        executor.runAsync(() -> {
            try {
                groupManagement.deleteGroup(fullPath);
                LOG.infof("Async group deleted: %s", fullPath);
            } catch (Exception e) {
                LOG.errorf(
                        "Async group deletion failed for %s: %s",
                        fullPath, e.getMessage()
                );
            }
        });
    }
}
