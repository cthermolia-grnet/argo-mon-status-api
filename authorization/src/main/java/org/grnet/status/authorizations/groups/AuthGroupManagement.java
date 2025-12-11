package org.grnet.status.authorizations.groups;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.grnet.status.authorizations.clients.KeycloakGroupClient;
import org.grnet.status.authorizations.clients.KeycloakTokenClient;
import org.grnet.status.authorizations.dtos.Group;
import org.grnet.status.authorizations.dtos.GroupRequest;
import org.jboss.logging.Logger;

import java.util.*;

@ApplicationScoped
public class AuthGroupManagement implements GroupManagement {

    private static final Logger LOG = Logger.getLogger(AuthGroupManagement.class);

    @Inject
    @RestClient
    KeycloakGroupClient groupClient;

    @Inject
    @RestClient
    KeycloakTokenClient tokenClient;

    @ConfigProperty(name = "auth.group.management.client-id")
    String clientId;

    @ConfigProperty(name = "auth.group.management.client-secret")
    String clientSecret;

    // ---------------------------------------------------------
    // CREATE GROUP
    // ---------------------------------------------------------
    @Override
    public void createGroup(String parentPath, String name, List<String> roles, Map<String, List<String>> attributes) {

        if (roles == null) roles = List.of("admin", "viewer");
        if (attributes == null) attributes = Map.of("description", List.of(name));

        // Build request
        var req = new GroupRequest();
        req.name = name;
        req.attributes = attributes;


        // Resolve parent group ID
        var parentId = getGroupIdByPath(parentPath);
        if (parentId == null)
            throw new IllegalStateException("Parent group not found: " + parentPath);

        // Create child group
        groupClient.createSubGroup(parentId, req);

        // Resolve new group ID (path-based)
        var newGroupPath = parentPath + "/" + name;
        var newGroupId = getGroupIdByPath(newGroupPath);
        if (newGroupId == null)
            throw new IllegalStateException("New group not found after creation: " + newGroupPath);

        LOG.infof("Group created: %s → ID: %s", newGroupPath, newGroupId);

        // Assign roles
        for (String role : roles) {
            groupClient.addRole(newGroupId, role);
        }

        // Update the group configuration with roles
        updateGroupConfigurationRoles(newGroupId, roles);

    }

    // ---------------------------------------------------------
    // DELETE GROUP
    // ---------------------------------------------------------
    @Override
    public void deleteGroup(String fullGroupPath) {
        try {
            var id = getGroupIdByPath(fullGroupPath);
            if (id != null) {
                groupClient.deleteGroup(id);
            } else {
                LOG.warnf("Group not found for deletion: %s", fullGroupPath);
            }
        } catch (Exception e) {
            LOG.errorf("Failed to delete group %s: %s", fullGroupPath, e.getMessage());
        }
    }

    // ---------------------------------------------------------
    // ADD ROLE
    // ---------------------------------------------------------
    @Override
    public void addRole(String groupId, String role) {
        groupClient.addRole(groupId, role);
    }

    // ---------------------------------------------------------
    // UPDATE CONFIGURATION (ROLES)
    // ---------------------------------------------------------
    @Override
    public void updateConfiguration(String groupId, List<String> roles) {
        updateGroupConfigurationRoles(groupId, roles);
    }

    private void updateGroupConfigurationRoles(String groupId, List<String> roles) {

        // Fetch full group structure
        var group = groupClient.getGroup(groupId);

        // Obtain defaultConfiguration ID
        var configId = group.attributes
                .get("defaultConfiguration")
                .get(0);

        // Fetch complete configuration JSON object
        var config = groupClient.getConfiguration(groupId, configId);

        // Remove default "member" role if present
        if (config.groupRoles != null) {
            config.groupRoles.remove("member");
        }

        // Assign new roles
        config.setGroupRoles(roles);

        // Update configuration
        groupClient.updateConfiguration(groupId, config);

        LOG.infof("Updated roles for group %s → %s", groupId, roles);
    }

    // ---------------------------------------------------------
    // TOKEN MANAGEMENT
    // ---------------------------------------------------------
    public String getAccessToken() {
        return tokenClient
                .getToken("client_credentials", clientId, clientSecret)
                .access_token;
    }

    // ---------------------------------------------------------
    // GROUP LOOKUP UTILITIES
    // ---------------------------------------------------------
    @Override
    public String getGroupId(String fullPath) {
        return getGroupIdByPath(fullPath);
    }

    // Internal lookup: resolves a group ID from the flattened groups map
    private String getGroupIdByPath(String fullPath) {
        return flattenGroups().get(fullPath);
    }

    // Builds a map of all groups (path → id, id → defaultConfigId) by flattening the Keycloak tree
    private Map<String, String> flattenGroups() {
        var response = groupClient.getGroups("");
        Map<String, String> map = new HashMap<>();

        for (Group group : response.results) {
            collectGroupRecursive(group, map);
        }
        return map;
    }

    // Recursively adds a group's path, id, and default configuration to the lookup map
    private void collectGroupRecursive(Group group, Map<String, String> map) {
        // Path → ID
        map.put(group.path, group.id);

        // ID → defaultConfiguration
        if (group.attributes != null && group.attributes.containsKey("defaultConfiguration")) {
            map.put(group.id, group.attributes.get("defaultConfiguration").get(0));
        }

        if (group.extraSubGroups != null) {
            for (Group child : group.extraSubGroups) {
                collectGroupRecursive(child, map);
            }
        }
    }
}
