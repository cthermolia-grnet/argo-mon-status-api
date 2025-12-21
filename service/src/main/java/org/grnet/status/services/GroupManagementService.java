package org.grnet.status.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.grnet.status.authorizations.dtos.GroupUser;
import org.grnet.status.authorizations.groups.GroupManagement;

import java.util.List;

@ApplicationScoped
public class GroupManagementService {
    @Inject
    GroupManagement groupManagement;

    @ConfigProperty(name = "api.auth.entitlements.parent.group")
    String parentGroup;


    public List<GroupUser> getMembers(String groupName) {

        var fullPath = normalizePath(parentGroup) + "/" + groupName;

        return groupManagement.fetchGroupMembers(fullPath);
    }

    public void addMember(String groupName, String username) {

        var fullPath = normalizePath(parentGroup) + "/" + groupName;

        groupManagement.addGroupMember(fullPath, username);
    }


    private static String normalizePath(String p) {
        if (p == null || p.isBlank()) return "";
        p = p.trim();

        // ensure leading slash
        if (!p.startsWith("/")) p = "/" + p;

        // remove trailing slash
        if (p.endsWith("/")) p = p.substring(0, p.length() - 1);

        return p;
    }
}
