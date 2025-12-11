package org.grnet.status.authorizations.clients;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.grnet.status.authorizations.dtos.GroupMembership;
import org.grnet.status.authorizations.dtos.GroupRequest;
import org.grnet.status.authorizations.dtos.GroupResponse;
import org.grnet.status.authorizations.exceptions.KeycloakExceptionMapper;
import org.grnet.status.authorizations.filters.BearerTokenRequestFilter;
import org.grnet.status.authorizations.dtos.Group;

@RegisterRestClient(configKey = "keycloak-group-client")
@Path("/agm/account")
@RegisterProvider(BearerTokenRequestFilter.class)
@RegisterProvider(KeycloakExceptionMapper.class)
public interface KeycloakGroupClient {

    // -------------------------------------------------------------
    // Fetch groups (search root hierarchy)
    // -------------------------------------------------------------
    @GET
    @Path("/group-admin/groups")
    GroupResponse getGroups(@QueryParam("search") String search);

    // -------------------------------------------------------------
    // Create subgroup
    // -------------------------------------------------------------
    @POST
    @Path("/group-admin/group/{id}/children")
    @Consumes(MediaType.APPLICATION_JSON)
    void createSubGroup(@PathParam("id") String parentId, GroupRequest request);

    // -------------------------------------------------------------
    // Get group
    // -------------------------------------------------------------
    @GET
    @Path("/group-admin/group/{id}/all")
    Group getGroup(@PathParam("id") String id);

    // -------------------------------------------------------------
    // Delete group
    // -------------------------------------------------------------
    @DELETE
    @Path("/group-admin/group/{id}")
    void deleteGroup(@PathParam("id") String id);

    // -------------------------------------------------------------
    // Add role to group
    // -------------------------------------------------------------
    @POST
    @Path("/group-admin/group/{id}/roles")
    void addRole(@PathParam("id") String id, @QueryParam("name") String role);

    // -------------------------------------------------------------
    // Update configuration (default config / roles)
    // -------------------------------------------------------------
    @POST
    @Path("/group-admin/group/{id}/configuration")
    @Consumes(MediaType.APPLICATION_JSON)
    void updateConfiguration(@PathParam("id") String id, GroupMembership config);

    // -------------------------------------------------------------
    // Get configuration (extended Keycloak)
    // -------------------------------------------------------------
    @GET
    @Path("/group-admin/group/{groupId}/configuration/{configId}")
    GroupMembership getConfiguration(@PathParam("groupId") String groupId,
                                     @PathParam("configId") String configId);
}
