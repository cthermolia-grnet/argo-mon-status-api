package org.grnet.status.services;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.UriInfo;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.grnet.status.authorizations.dtos.GroupUser;
import org.grnet.status.authorizations.dtos.GroupUserResponse;
import org.grnet.status.authorizations.dtos.PartialGroup;
import org.grnet.status.authorizations.groups.GroupManagement;
import org.grnet.status.authorizations.groups.GroupMembersResponse;
import org.grnet.status.dtos.pagination.PageResource;
import org.grnet.status.entities.Page;
import org.grnet.status.entities.PageQueryImpl;
import org.grnet.status.repositories.TenantRepository;
import org.grnet.status.util.Utility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ApplicationScoped
public class GroupManagementService {

    @Inject
    GroupManagement groupManagement;

    @Inject
    TenantRepository tenantRepository;

    @Inject
    Utility utility;

    @Inject
    MailerService mailerService;

    @ConfigProperty(name = "api.auth.entitlements.parent.group")
    String parentGroup;

    @ConfigProperty(name = "api.ui.url")
    String uiBaseUrl;

    @ConfigProperty(name = "api.auth.entitlements.parent.group")
    String namespace;

    public PageResource<GroupUserResponse> getAllMembers(String groupName, String search, int page, int size, UriInfo uriInfo) {

        var response = getMembers(groupName, page*size, size, search);

        var members = response
                .results
                .stream()
                .map(g->g.user)
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
                .toList();

        var pageable = new PageQueryImpl<GroupUserResponse>();

        pageable.list = members;
        pageable.index = page;
        pageable.size = size;
        pageable.count = response.count;
        pageable.page = Page.of(page, size);

        return new PageResource<>(pageable, uriInfo);
    }

    public GroupMembersResponse getMembers(String groupName, int first, int max, String search) {

        var fullPath = normalizePath(parentGroup) + "/" + groupName;

        return groupManagement.fetchGroupMembers(fullPath, first, max, search);
    }

    public List<GroupUser> getTenantMembersByRole(String groupName, String role) {

        var fullPath = normalizePath(parentGroup) + "/tenants/" + groupName;

        return groupManagement.fetchGroupMembersByRole(fullPath, role);
    }

    public void addMember(String groupName, String username) {

        var fullPath = normalizePath(parentGroup) + "/" + groupName;

        groupManagement.addGroupMember(fullPath, username, "member");
    }

    public void addUserToTenantGroup(String tenantName, String username, String role) {

        var parentPath = "/" + namespace + "/tenants/" + tenantName;

        groupManagement.addGroupMember(parentPath, username, role);
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

    public void addMemberToGroup(String tenantId, String username, String role, String email){

        var tenant = tenantRepository.findById(tenantId);

        var parentPath = "/" + namespace + "/tenants/"+tenant.name;

        groupManagement.addGroupMember(parentPath, username, role);

        try {
            mailerService.sendEmailToMemberAddedGroup(
                    List.of(email),
                    tenant.name,
                    role,
                    uiBaseUrl
            );
        } catch (Exception e) {
            Log.warn("Added to group email failed: " + email, e);
        }
    }

    public void deleteMemberFromGroup(String tenantId, String memberId){

        var tenant = tenantRepository.findById(tenantId);

        var parentPath = "/" + namespace + "/tenants/"+tenant.name;

        groupManagement.removeMemberFromGroup(parentPath, memberId);
    }
}
