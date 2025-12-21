package org.grnet.status.authorizations.groups;

import io.quarkus.arc.profile.IfBuildProfile;
import jakarta.enterprise.context.ApplicationScoped;
import org.grnet.status.authorizations.dtos.*;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.Map;

@ApplicationScoped
@IfBuildProfile(anyOf = {"dev", "test"})
public class DevGroupManagement implements GroupManagement {

    private static final Logger LOG = Logger.getLogger(DevGroupManagement.class);

    @Override
    public void createGroup(String parentPath, String name, List<String> roles, Map<String, List<String>> attributes) {
        LOG.debugf("DEV: createGroup skipped (%s/%s)", parentPath, name);
    }

    @Override
    public void deleteGroup(String fullGroupPath) {
        LOG.debugf("DEV: deleteGroup skipped (%s)", fullGroupPath);
    }

    @Override
    public List<GroupUser> fetchGroupMembers(String fullPath) {
        LOG.debugf("DEV: fetchGroupMembers returns empty (%s)", fullPath);
        return List.of();
    }

    @Override
    public void addRole(String groupId, String role) {
        LOG.debugf("DEV: addRole skipped (%s role=%s)", groupId, role);
    }

    @Override
    public String getGroupId(String fullPath) {
        LOG.debugf("DEV: getGroupId skipped (%s)", fullPath);
        return null;
    }

    @Override
    public void updateConfiguration(String groupId, List<String> groupRoles) {
        LOG.debugf("DEV: updateConfiguration skipped (%s)", groupId);
    }

    @Override
    public void addGroupMember(String fullPath, String username) {
        LOG.debugf("DEV: addGroupMemberIdempotent skipped (user=%s, group=%s)", username, fullPath);
    }
}