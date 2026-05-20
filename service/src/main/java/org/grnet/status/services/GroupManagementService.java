package org.grnet.status.services;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.UriInfo;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import org.grnet.endpoint.scanner.runtime.clients.groupmanagement.AuthGroupManagement;
import org.grnet.endpoint.scanner.runtime.clients.groupmanagement.GroupManagement;
import org.grnet.endpoint.scanner.runtime.clients.groupmanagement.response.*;
import org.grnet.status.dtos.pagination.PageResource;
import org.grnet.status.entities.Page;
import org.grnet.status.entities.PageQueryImpl;
import org.grnet.status.enums.resources.TenantResource;
import org.grnet.status.repositories.TenantRepository;
import org.grnet.status.util.Utility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Service responsible for managing groups and their members through the external authorization provider.
 */
@ApplicationScoped
public class GroupManagementService {

    @Inject
    AuthGroupManagement groupManagement;

    @Inject
    TenantRepository tenantRepository;

    @Inject
    Utility utility;

    @Inject
    MailerService mailerService;

    @ConfigProperty(name = "api.auth.entitlements.parent-group")
    String parentGroup;

    @ConfigProperty(name = "api.ui.url")
    String uiBaseUrl;

    @ConfigProperty(name = "api.auth.entitlements.parent-group")
    String namespace;

    /**
     * Returns a paginated list of members for the given group.
     *
     * @param groupName the group identifier
     * @param search optional search filter
     * @param page 0-based page index
     * @param size number of records per page
     * @param uriInfo request context for pagination links
     * @return paginated list of group members
     */
    public PageResource<GroupUserResponse> getAllMembers(String groupName, String search, int page, int size, UriInfo uriInfo) {

        var response = getMembers(groupName, page*size, size, search);

        var members = response
                .results
                .stream()
                .map(g->g.user)
                .map(gu -> {

                    List<UserGroupInfoDto> list= new ArrayList();
                    if (gu.attributes != null && gu.attributes.getLocalEntitlements() != null) {

                        list = CDI.current()
                                .select(UserEntitlementsService.class)
                                .get()
                                .parseLocalEntitlements(gu.attributes.getLocalEntitlements(), "tenant_admin", TenantResource.TENANT.resourceName());
                    }


                    var user = new GroupUserResponse();
                    user.id = gu.id;
                    user.email = gu.email;
                    user.username = gu.username;
                    user.firstName = gu.firstName;
                    user.lastName = gu.lastName;
                    user.uid = gu.getUid();
                    user.tenants = list;
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

    /**
     * Fetches members of a group directly from the authorization provider.
     *
     * @param groupName the group identifier
     * @param first starting index (offset)
     * @param max maximum number of results
     * @param search optional search filter
     * @return provider response containing members and total count
     */
    public GroupMembersResponse getMembers(String groupName, int first, int max, String search) {

        var fullPath = normalizePath(parentGroup) + "/" + groupName;

        return groupManagement.fetchGroupMembers(fullPath, first, max, search);
    }

    /**
     * Returns members of a tenant group filtered by role.
     *
     * @param resource Resource name
     * @param tenantId The tenant id
     * @return list of users with the specified role
     */
    public List<GroupUser> getTenantMembersByRole(String resource, String tenantId) {

        var fullPath = normalizePath(parentGroup) + "/tenant_admin/" + resource+ "/"+tenantId;

        return groupManagement.fetchGroupMembersByRole(fullPath, "member");
    }

    /**
     * Adds a user to a group with the default role "member".
     *
     * @param groupName the group identifier
     * @param username user identifier recognized by the auth provider
     */
    public void addMember(String groupName, String username) {

        var fullPath = normalizePath(parentGroup) + "/" + groupName;

        groupManagement.addGroupMember(fullPath, username, "member");
    }

    /**
     * Adds a user to a tenant group with a specific role.
     *
     * @param tenantName tenant name
     * @param username user identifier
     * @param role role to assign
     */
    public void addUserToTenantGroup(String tenantName, String username, String role) {

        var parentPath = "/" + namespace + "/tenants/" + tenantName;

        groupManagement.addGroupMember(parentPath, username, role);
    }

    /**
     * Normalizes a group path.
     *
     * @param p raw path
     * @return normalized path
     */
    private static String normalizePath(String p) {
        if (p == null || p.isBlank()) return "";
        p = p.trim();

        // ensure leading slash
        if (!p.startsWith("/")) p = "/" + p;

        // remove trailing slash
        if (p.endsWith("/")) p = p.substring(0, p.length() - 1);

        return p;
    }

    /**
     * Returns a paginated list of groups from the authorization provider.
     *
     * @param page 0-based page index
     * @param size page size
     * @param uriInfo request context for pagination links
     * @return paginated list of groups
     */
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
