package org.grnet.status.authorizations.groups;

import org.grnet.status.authorizations.dtos.Group;
import org.grnet.status.authorizations.dtos.GroupUser;
import org.grnet.status.authorizations.dtos.PartialGroup;

import java.util.List;
import java.util.Map;

public interface GroupManagement {

    /**
     * Creates a subgroup under the given parent path.
     *
     * @param parentPath The full Keycloak path
     * @param name       The new group name
     * @param roles      A list of default roles to assign (optional)
     * @param attributes Optional string attributes (Keycloak metadata)
     */
    void createGroup(String parentPath, String name, List<String> roles, Map<String, List<String>> attributes);

    /** Deletes a group by full path. */
    void deleteGroup(String fullGroupPath);

    /** Fetch members of a group. */
    GroupMembersResponse fetchGroupMembers(String groupPath, int first, int max, String search);

    /** Assign one role to an existing group. */
    void addRole(String groupId, String role);

    /** Get group ID from full hierarchical path. */
    String getGroupId(String fullPath);

    /** Update configuration of a group (generic Keycloak extension). */
    void updateConfiguration(String groupId, List<String> groupRoles);

    List<GroupUser> fetchGroupMembersByRole(String fullPath, String role);

    void addGroupMember(String fullPath, String username, String role);

    void addMemberToGroupByGroupId(String id, String username, String role);

    List<PartialGroup> fetchGroups();

    void removeMemberFromGroup(String fullPath, String memberId);

    default void collectGroupRecursive(Group group, List<PartialGroup> groups) {

        groups.add(new PartialGroup(group.id, group.name, group.path, group.groupRoles));

        if (group.extraSubGroups != null) {
            for (Group child : group.extraSubGroups) {
                collectGroupRecursive(child, groups);
            }
        }
    }
}
