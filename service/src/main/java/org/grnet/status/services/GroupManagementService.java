package org.grnet.status.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.UriInfo;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.grnet.status.authorizations.dtos.GroupUser;
import org.grnet.status.authorizations.dtos.GroupUserResponse;
import org.grnet.status.authorizations.dtos.PartialGroup;
import org.grnet.status.authorizations.groups.GroupManagement;
import org.grnet.status.dtos.pagination.PageResource;
import org.grnet.status.entities.Page;
import org.grnet.status.entities.PageQueryImpl;
import org.grnet.status.util.Utility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static io.netty.util.AsciiString.contains;

@ApplicationScoped
public class GroupManagementService {

    @Inject
    GroupManagement groupManagement;

    @Inject
    Utility utility;

    @ConfigProperty(name = "api.auth.entitlements.parent.group")
    String parentGroup;

    public PageResource<GroupUser> getAllMembers(String groupName, String search, int page, int size, UriInfo uriInfo) {

        var members = getMembers(groupName);

        // search
        if (StringUtils.isNotBlank(search)) {

            var lowerSearch = search.toLowerCase();
            members = members.stream()
                    .filter(u ->    contains(u.id, lowerSearch) ||
                                    contains(u.username, lowerSearch) ||
                                    contains(u.email, lowerSearch) ||
                                    contains(u.firstName, lowerSearch) ||
                                    contains(u.lastName, lowerSearch))
                    .toList();

            members = new ArrayList<>(members);
        }

        var partition = utility.partition(new ArrayList<>(members), size);

        var pageableMembers = partition.get(page) == null ? Collections.EMPTY_LIST : partition.get(page);

        var pageable = new PageQueryImpl<GroupUser>();

        pageable.list = pageableMembers;
        pageable.index = page;
        pageable.size = size;
        pageable.count = members.size();
        pageable.page = Page.of(page, size);

        return new PageResource<>(pageable, uriInfo);
    }

    public List<GroupUserResponse> getMembers(String groupName) {

        var fullPath = normalizePath(parentGroup) + "/" + groupName;

        return groupManagement
                .fetchGroupMembers(fullPath)
                .stream()
                .map(gu -> {
                    var user = new GroupUserResponse();
                    user.id = gu.id;
                    user.email = gu.email;
                    user.username = gu.username;
                    user.firstName = gu.firstName;
                    user.lastName = gu.lastName;
                    user.tenants = gu.getTenants();
                    return user;
                })
                .collect(Collectors.toList());
    }

    public List<GroupUser> getTenantMembersByRole(String groupName, String role) {

        var fullPath = normalizePath(parentGroup) + "/tenants/" + groupName;

        return groupManagement.fetchGroupMembersByRole(fullPath, role);
    }

    public void addMember(String groupName, String username) {

        var fullPath = normalizePath(parentGroup) + "/" + groupName;

        groupManagement.addGroupMember(fullPath, username, "member");
    }

    public void addUserToGroup(String id, String username, String role) {

        groupManagement.addMemberToGroupByGroupId(id, username, role);
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

    public PageResource<PartialGroup> fetchGroups(int page, int size, UriInfo uriInfo){

        var groups = groupManagement.fetchGroups();

        var partition = utility.partition(new ArrayList<>(groups), size);

        var pageableMembers = partition.get(page) == null ? Collections.EMPTY_LIST : partition.get(page);

        var pageable = new PageQueryImpl<PartialGroup>();

        pageable.list = pageableMembers;
        pageable.index = page;
        pageable.size = size;
        pageable.count = groups.size();
        pageable.page = Page.of(page, size);

        return new PageResource<>(pageable, uriInfo);
    }
}
