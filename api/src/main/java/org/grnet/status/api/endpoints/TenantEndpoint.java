package org.grnet.status.api.endpoints;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeIn;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.grnet.status.api.resolvers.CheckDateFormat;
import org.grnet.status.api.resolvers.TenantNameResolver;
import org.grnet.status.authorizations.dtos.GroupUser;
import org.grnet.status.authorizations.interceptors.CheckEntitlements;
import org.grnet.status.authorizations.interceptors.Resolver;
import org.grnet.status.constraints.NotFoundEntity;
import org.grnet.status.dtos.InformativeResponse;
import org.grnet.status.dtos.pagination.PageResource;
import org.grnet.status.dtos.profile.aggregation.AggregationProfileResponse;
import org.grnet.status.dtos.profile.metric.MetricProfileResponse;
import org.grnet.status.dtos.profile.operation.OperationProfileResponse;
import org.grnet.status.dtos.project.ProjectResponseDto;
import org.grnet.status.dtos.report.FullReportResponseDto;
import org.grnet.status.dtos.tenant.TenantRequestDto;
import org.grnet.status.dtos.tenant.TenantResponseDto;
import org.grnet.status.dtos.tenant.invitations.TenantInvitationRequest;
import org.grnet.status.dtos.tenant.invitations.TenantInvitationResponse;
import org.grnet.status.repositories.TenantInvitationRepository;
import org.grnet.status.repositories.TenantRepository;
import org.grnet.status.services.ProfileService;
import org.grnet.status.services.*;
import org.grnet.status.util.Utility;

import java.io.IOException;
import java.util.List;

import static org.eclipse.microprofile.openapi.annotations.enums.ParameterIn.QUERY;

@Path("/v1/tenants")
@Authenticated
@Tag(name = "Tenant")
@SecurityScheme(
        securitySchemeName = "Authentication",
        description = "JWT token",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER)
@CheckEntitlements(group = "tenants")
public class TenantEndpoint {

    @Inject
    Utility utility;

    @Inject
    TenantService tenantService;

    @Inject
    TenantProjectService tenantProjectService;

    @Inject
    TenantInvitationService tenantInvitationService;

    @Inject
    ReportService reportService;

    @Inject
    GroupManagementService groupManagementService;

    @Inject
    ProfileService profileService;

    @Operation(
            summary = "List Tenants Available to the User",
            description = "Retrieves a paginated list of tenants the authenticated user is allowed to access."
    )
    @APIResponse(
            responseCode = "200",
            description = "Tenants list retrieved successfully.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = PageableTenants.class))
    )
    @APIResponse(
            responseCode = "401",
            description = "User has not been authenticated.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class))
    )
    @APIResponse(
            responseCode = "403",
            description = "Not permitted.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class))
    )
    @APIResponse(
            responseCode = "500",
            description = "Internal Server Error.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class))
    )
    @SecurityRequirement(name = "Authentication")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @CheckEntitlements(byPassAuthorization = true)
    public Response listTenants(
            @Parameter(name = "search", in = QUERY,
                    description = "Search tenants by name.")
            @QueryParam("search") String search,
            @Parameter(name = "sort", in = QUERY,
                    schema = @Schema(type = SchemaType.STRING, defaultValue = ""),
                    examples = {
                            @ExampleObject(name = "Tenant Name", value = "name"),
                            @ExampleObject(name = "Created At", value = "createdAt")},
                    description = "The field used to sort the results.")
            @DefaultValue("createdAt")
            @QueryParam("sort")
            String sort,
            @Parameter(name = "order", in = QUERY,
                    schema = @Schema(type = SchemaType.STRING),
                    examples = {
                            @ExampleObject(name = "Ascending", value = "ASC"),
                            @ExampleObject(name = "Descending", value = "DESC")},
                    description = "The order of the sorted results.")
            @DefaultValue("DESC")
            @QueryParam("order")
            String order,
            @Parameter(name = "page", in = QUERY,
                    description = "Page number. Must be >= 1.")
            @DefaultValue("1")
            @Min(value = 1, message = "Page number must be >= 1.")
            @QueryParam("page")
            int page,
            @Parameter(name = "size", in = QUERY,
                    description = "Page size.")
            @DefaultValue("10")
            @Min(value = 1, message = "Page size must be between 1 and 100.")
            @Max(value = 100, message = "Page size must be between 1 and 100.")
            @QueryParam("size")
            int size, @Context UriInfo uriInfo) {

        var result = tenantService.listAuthorizedTenants(page - 1, size, uriInfo, search, sort, order);

        return Response.ok().entity(result).build();
    }

    @Operation(
            summary = "Get Tenant By Id .",
            description = "Returns a specific tenant assessment.")
    @APIResponse(
            responseCode = "200",
            description = "The corresponding tenant.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = TenantResponseDto.class)))
    @APIResponse(
            responseCode = "401",
            description = "User has not been authenticated.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "403",
            description = "Not permitted.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "404",
            description = "Entity Not Found.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "500",
            description = "Internal Server Error.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @SecurityRequirement(name = "Authentication")
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @CheckEntitlements(role = "viewer", resolvers = {
            @Resolver( idResolver = TenantNameResolver.class, pathId = "id")
    })
    public Response getTenant(
            @Parameter(description = "The ID of the tenant to retrieve.",
                    required = true,
                    example = "6f9ff5ff-nn9g-4378-9200-5rf6719n6vg4",
                    schema = @Schema(type = SchemaType.STRING))
            @PathParam("id")
            @Valid @NotFoundEntity(repository = TenantRepository.class, message = "There is no Tenant with the following id: ") String id) {

        var tenant = tenantService.getTenantById(id);

        return Response.ok().entity(tenant).build();
    }

    @Operation(
            summary = "Update a tenant.",
            description = "Updates a specific tenant."
    )
    @APIResponse(
            responseCode = "200",
            description = "Tenant updated successfully.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = TenantResponseDto.class))
    )
    @APIResponse(
            responseCode = "401",
            description = "User has not been authenticated.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class))
    )
    @APIResponse(
            responseCode = "404",
            description = "Page not found.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class))
    )
    @APIResponse(
            responseCode = "409",
            description = "Tenant already exists.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class))
    )
    @APIResponse(
            responseCode = "500",
            description = "Internal Server Error.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class))
    )

    @PUT
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @CheckEntitlements(role = "admin", resolvers = {
            @Resolver( idResolver = TenantNameResolver.class, pathId = "id")
    })
    public Response updateTenant(
            @Parameter(
                    description = "The ID of the tenant to retrieve.",
                    required = true,
                    example = "6f9ff5ff-nn9g-4378-9200-5rf6719n6vg4",
                    schema = @Schema(type = SchemaType.STRING))
            @Valid @NotFoundEntity(repository = TenantRepository.class, message = "There is no Tenant with the following id: ") String id,

            @Valid @NotNull(message = "The request body is empty.") TenantRequestDto request) throws IOException {

        var updated = tenantService.updateTenant(id, request);
        return Response.ok().entity(updated).build();
    }

    @Operation(summary = "List projects added to tenant",
            description = "Retrieves a list of projects that tenant belongs")
    @APIResponse(
            responseCode = "200",
            description = "Tenants list retrieved",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = ProjectResponseDto.class)))
    @APIResponse(
            responseCode = "401",
            description = "User has not been authenticated.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "403",
            description = "Not permitted.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "404",
            description = "Project does not exist.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "500",
            description = "Internal Server Error.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @SecurityRequirement(name = "Authentication")
    @GET
    @Path("/{id}/projects")
    @CheckEntitlements(role = "admin", resolvers = {
            @Resolver( idResolver = TenantNameResolver.class, pathId = "id")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProjectsByTenant(
            @Parameter(
                    description = "The ID of the project to retrieve.",
                    required = true,
                    example = "6f9ff5ff-nn9g-4378-9200-5rf6719n6vg4",
                    schema = @Schema(type = SchemaType.STRING))
            @PathParam("id")
            @Valid @NotFoundEntity(repository = TenantRepository.class, message = "There is no Tenant with the following id: ") String id,
            @Parameter(name = "search", in = QUERY,
                    description = "The \"search\" parameter is a query parameter that allows clients to specify a text string that will be used to search for matches in specific fields in Project entity. " +
                            "The search will be conducted in the following fields : projects' name. ")
            @QueryParam("search") String search,
            @Parameter(name = "sort", in = QUERY,
                    schema = @Schema(type = SchemaType.STRING, defaultValue = ""),
                    examples = {
                            @ExampleObject(name = "Project name", value = "name"),
                            @ExampleObject(name = "Created At", value = "createdAt")},
                    description = "The \"sort\" parameter allows clients to specify the field by which they want the results to be sorted.")
            @DefaultValue("createdAt")
            @QueryParam("sort")
            String sort,
            @Parameter(name = "order", in = QUERY,
                    schema = @Schema(type = SchemaType.STRING, defaultValue = ""),
                    examples = {@ExampleObject(name = "Ascending", value = "ASC"), @ExampleObject(name = "Descending", value = "DESC")},
                    description = "The \"order\" parameter specifies the order in which the sorted results should be returned.") @DefaultValue("DESC")
            @QueryParam("order")
            String order,
            @Parameter(name = "page", in = QUERY,
                    description = "Indicates the page number. Page number must be >= 1.") @DefaultValue("1") @Min(value = 1, message = "Page number must be >= 1.")
            @QueryParam("page")
            int page,
            @Parameter(name = "size", in = QUERY,
                    description = "The page size.")
            @DefaultValue("10") @Min(value = 1, message = "Page size must be between 1 and 100.") @Max(value = 100, message = "Page size must be between 1 and 100.")
            @QueryParam("size")
            int size,
            @Context UriInfo uriInfo) {

        var project = tenantProjectService.getProjectsByTenant(id, page - 1, size, uriInfo, search, sort, order);

        return Response.ok().entity(project).build();
    }

    @Operation(summary = "List tenant members.",
            description = "Retrieves a list of tenant members and related metadata.")
    @APIResponse(
            responseCode = "200",
            description = "Tenant members list retrieved successfully.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = PageableTenantMembers.class)))
    @APIResponse(
            responseCode = "401",
            description = "User has not been authenticated.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "403",
            description = "Not permitted.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "404",
            description = "Tenant does not exist.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "500",
            description = "Internal Server Error.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @SecurityRequirement(name = "Authentication")
    @GET
    @Path("/{id}/members")
    @CheckEntitlements(role = "admin", resolvers = {
            @Resolver( idResolver = TenantNameResolver.class, pathId = "id")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMembersByTenant(
            @Parameter(
                    description = "The ID of the tenant to retrieve.",
                    required = true,
                    example = "6f9ff5ff-nn9g-4378-9200-5rf6719n6vg4",
                    schema = @Schema(type = SchemaType.STRING))
            @PathParam("id")
            @Valid @NotFoundEntity(repository = TenantRepository.class, message = "There is no Tenant with the following id: ") String id,
            @Parameter(name = "page", in = QUERY,
                    description = "Indicates the page number. Page number must be >= 1.") @DefaultValue("1") @Min(value = 1, message = "Page number must be >= 1.")
            @QueryParam("page")
            int page,
            @Parameter(name = "size", in = QUERY,
                    description = "The page size.")
            @DefaultValue("10") @Min(value = 1, message = "Page size must be between 1 and 100.") @Max(value = 100, message = "Page size must be between 1 and 100.")
            @QueryParam("size")
            int size,
            @Context UriInfo uriInfo) {

        var members = tenantProjectService.getMembersByTenant(id, page - 1, size, uriInfo);

        return Response.ok().entity(members).build();
    }

    @Tag(name = "Tenant")
    @Operation(summary = "Invite a user to be a member of tenant.",
            description = "Invite a user to be a member of tenant.")
    @APIResponse(
            responseCode = "200",
            description = "Mail send",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = TenantInvitationResponse.class)))
    @APIResponse(
            responseCode = "401",
            description = "User has not been authenticated.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "403",
            description = "Not permitted.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "409",
            description = "Invitation already exists.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "500",
            description = "Internal Server Error.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @POST
    @Path("/{id}/invitation")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @CheckEntitlements(role = "admin", resolvers = {
            @Resolver( idResolver = TenantNameResolver.class, pathId = "id")
    })
    public Response notifyAms(
            @Parameter(
                    description = "The ID of the tenant to create an invitation.",
                    required = true,
                    example = "c242e43f-9869-4fb0-b881-631bc5746ec0",
                    schema = @Schema(type = SchemaType.STRING))
            @PathParam("id")
            @Valid @NotFoundEntity(repository = TenantRepository.class, message = "There is no Tenant with the following id: ") String id,
            @Valid @NotNull(message = "The request body is empty.")
            TenantInvitationRequest request) {

        var status = tenantInvitationService.createInvitation(id, request, utility.getUserUniqueIdentifier());
        return Response.ok().entity(status).build();
    }

    @Tag(name = "Tenant")
    @Operation(
            summary = "Get all invitations of tenant.",
            description = "Returns all invitation of a Tenant. "
    )
    @APIResponse(
            responseCode = "200",
            description = "Invitation details.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = PageableTenantInvitations.class)))
    @APIResponse(
            responseCode = "401",
            description = "User has not been authenticated.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "403",
            description = "Not permitted.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "404",
            description = "Invitation not found.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "410",
            description = "Invitation expired.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @GET
    @Path("/{id}/invitations")
    @Produces(MediaType.APPLICATION_JSON)
    @CheckEntitlements(role = "admin", resolvers = {
            @Resolver( idResolver = TenantNameResolver.class, pathId = "id")
    })
    public Response getInvitations(
            @Parameter(
                    description = "The ID of the tenant under which the invitation was created.",
                    required = true,
                    example = "c242e43f-9869-4fb0-b881-631bc5746ec0",
                    schema = @Schema(type = SchemaType.STRING))
            @PathParam("id")
            String id,
            @Parameter(name = "search", in = QUERY,
                    description = "Search invitations by role or email.")
            @QueryParam("search") String search,
            @Parameter(name = "sort", in = QUERY,
                    schema = @Schema(type = SchemaType.STRING, defaultValue = "createdAt"),
                    examples = {
                            @ExampleObject(name = "Created At", value = "createdAt"),
                            @ExampleObject(name = "Email", value = "email"),
                            @ExampleObject(name = "Status", value = "status")
                    },
                    description = "The field used to sort the results.")
            @DefaultValue("createdAt")
            @QueryParam("sort") String sort,
            @Parameter(
                    name = "order",
                    in = QUERY,
                    schema = @Schema(type = SchemaType.STRING, defaultValue = "DESC"),
                    examples = {
                            @ExampleObject(name = "Ascending", value = "ASC"),
                            @ExampleObject(name = "Descending", value = "DESC")
                    },
                    description = "The order of the sorted results.")
            @DefaultValue("DESC")
            @QueryParam("order") String order,
            @Parameter(name = "page", in = QUERY,
                    description = "Page number. Must be >= 1.")
            @DefaultValue("1")
            @Min(value = 1, message = "Page number must be >= 1.")
            @QueryParam("page")
            int page,
            @Parameter(name = "size", in = QUERY,
                    description = "Page size.")
            @DefaultValue("10")
            @Min(value = 1, message = "Page size must be between 1 and 100.")
            @Max(value = 100, message = "Page size must be between 1 and 100.")
            @QueryParam("size")
            int size, @Context UriInfo uriInfo)
    {
        var response = tenantInvitationService.getInvitationsByTenantByPageAndSize(search, sort, order, id,page - 1, size, uriInfo);

        return Response.ok(response).build();
    }

    @Tag(name = "Tenant")
    @Operation(
            summary = "Revoke an invitation invitation.",
            description = "Revoke an invitation."
    )
    @APIResponse(
            responseCode = "200",
            description = "Invitation updated.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = TenantInvitationResponse.class)))
    @APIResponse(
            responseCode = "401",
            description = "User has not been authenticated.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "403",
            description = "Not permitted.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "404",
            description = "Invitation not found.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "409",
            description = "Invitation already responded.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "410",
            description = "Invitation expired.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @SecurityRequirement(name = "Authentication")
    @PATCH
    @Path("/{id}/invitations/{invitation_id}")
    @CheckEntitlements(role = "admin", resolvers = {
            @Resolver( idResolver = TenantNameResolver.class, pathId = "id")
    })
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response revoke(
            @PathParam("id")
            @Valid @NotFoundEntity(repository = TenantRepository.class, message = "There is no Invitation with the following id: ")
            String id,
            @PathParam("invitation_id")
            @Valid @NotFoundEntity(repository = TenantInvitationRepository.class, message = "There is no Invitation with the following invitation_id: ")
            String invitationId) {

        var response = tenantInvitationService.revokeInvitation(id, invitationId, utility.getUserUniqueIdentifier());

        return Response.ok(response).build();
    }

    @Tag(name = "Tenant")
    @Operation(
            summary = "Delete member from a tenant group.",
            description = "Delete member from a tenant group.")
    @APIResponse(
            responseCode = "200",
            description = "Member deleted successfully from a tenant group.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "401",
            description = "User has not been authenticated.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "403",
            description = "Not permitted.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "404",
            description = "Entity Not Found.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "500",
            description = "Internal Server Error.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @SecurityRequirement(name = "Authentication")
    @DELETE
    @Path("/{id}/members/{member_id}")
    @Produces(MediaType.APPLICATION_JSON)
    @CheckEntitlements(role = "admin", resolvers = {
            @Resolver( idResolver = TenantNameResolver.class, pathId = "id")
    })
    @Authenticated
    public Response deleteMemberFromGroup(
            @Parameter(description = "The ID of the tenant.",
                    required = true,
                    example = "c242e43f-9869-4fb0-b881-631bc5746ec0",
                    schema = @Schema(type = SchemaType.STRING))
            @PathParam("id")
            @Valid @NotFoundEntity(repository = TenantRepository.class, message = "There is no Tenant with the following id: ") String id,
            @Parameter(description = "The member's id.",
                    required = true,
                    example = "r682e43f-4569-4fb0-b865-631bc5746ec0",
                    schema = @Schema(type = SchemaType.STRING))
            @PathParam("member_id") String memberId) {

        groupManagementService.deleteMemberFromGroup(id, memberId);

        var response = new InformativeResponse();
        response.code = 200;
        response.message = "Member deleted successfully from group.";

        return Response.ok().entity(response).build();
    }

    @Tag(name = "Aggregation Profiles")
    @Operation(
            summary = "Get a specific aggregation profile.",
            description = "List one specific operations profile targeted by it's unique id.")
    @APIResponse(
            responseCode = "200",
            description = "Aggregation profile found.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = AggregationProfileResponse.class)))
    @APIResponse(
            responseCode = "401",
            description = "User has not been authenticated.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "403",
            description = "Not permitted.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "404",
            description = "Aggregation profile not found.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "500",
            description = "Internal Server Error.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @SecurityRequirement(name = "Authentication")
    @GET
    @Path("/{id}/aggregation-profiles/{profile_id}")
    @Produces(MediaType.APPLICATION_JSON)
    @CheckEntitlements(role = "admin", resolvers = {
            @Resolver( idResolver = TenantNameResolver.class, pathId = "id")
    })
    @Authenticated
    public Response listSpecificAggregationProfiles(
            @Parameter(description = "The ID of the tenant.",
                    required = true,
                    example = "c242e43f-9869-4fb0-b881-631bc5746ec0",
                    schema = @Schema(type = SchemaType.STRING))
            @PathParam("id")
            @Valid @NotFoundEntity(repository = TenantRepository.class, message = "There is no Tenant with the following id: ") String id,
            @Parameter(description = "The aggregation profile id.",
                    required = true,
                    example = "profile-id",
                    schema = @Schema(type = SchemaType.STRING))
            @PathParam("profile_id") String profileId,
            @Parameter(name = "date", in = QUERY, description = "Target date to retrieve a historic version of the profile.") @QueryParam("date")
            @Valid @CheckDateFormat(pattern = "yyyy-mm-dd", message = "Valid date format is yyyy-mm-dd.") String date) {

        var response = profileService.listSpecificAggregationProfiles(id, profileId, date);

        return Response.ok().entity(response).build();
    }

    @Tag(name = "Aggregation Profiles")
    @Operation(
            summary = "List all aggregation profiles.",
            description = "List all aggregation profiles.")
    @APIResponse(
            responseCode = "200",
            description = "List of aggregation profiles.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = AggregationProfileResponse.class)))
    @APIResponse(
            responseCode = "401",
            description = "User has not been authenticated.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "403",
            description = "Not permitted.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "404",
            description = "Tenant not found.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "500",
            description = "Internal Server Error.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @SecurityRequirement(name = "Authentication")
    @GET
    @Path("/{id}/aggregation-profiles")
    @Produces(MediaType.APPLICATION_JSON)
    @CheckEntitlements(role = "admin", resolvers = {
            @Resolver( idResolver = TenantNameResolver.class, pathId = "id")
    })
    @Authenticated
    public Response listAllAggregationProfiles(
            @Parameter(description = "The ID of the tenant.",
                    required = true,
                    example = "c242e43f-9869-4fb0-b881-631bc5746ec0",
                    schema = @Schema(type = SchemaType.STRING))
            @PathParam("id")
            @Valid @NotFoundEntity(repository = TenantRepository.class, message = "There is no Tenant with the following id: ") String id,
            @Parameter(name = "date", in = QUERY, description = "Target date to retrieve a historic version of the profile.") @QueryParam("date")
            @Valid @CheckDateFormat(pattern = "yyyy-mm-dd", message = "Valid date format is yyyy-mm-dd.") String date) {

        var response = profileService.listAllAggregationProfiles(id, date);

        return Response.ok().entity(response).build();
    }

    @Tag(name = "Metric Profiles")
    @Operation(
            summary = "Get a specific metric profile.",
            description = "List one specific metric profile targeted by it's unique id.")
    @APIResponse(
            responseCode = "200",
            description = "Metric profile found.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = MetricProfileResponse.class)))
    @APIResponse(
            responseCode = "401",
            description = "User has not been authenticated.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "403",
            description = "Not permitted.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "404",
            description = "Metric profile not found.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "500",
            description = "Internal Server Error.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @SecurityRequirement(name = "Authentication")
    @GET
    @Path("/{id}/metric-profiles/{profile_id}")
    @Produces(MediaType.APPLICATION_JSON)
    @CheckEntitlements(role = "admin", resolvers = {
            @Resolver( idResolver = TenantNameResolver.class, pathId = "id")
    })
    @Authenticated
    public Response listSpecificMetricProfiles(
            @Parameter(description = "The ID of the tenant.",
                    required = true,
                    example = "c242e43f-9869-4fb0-b881-631bc5746ec0",
                    schema = @Schema(type = SchemaType.STRING))
            @PathParam("id")
            @Valid @NotFoundEntity(repository = TenantRepository.class, message = "There is no Tenant with the following id: ") String id,
            @Parameter(description = "The metric profile id.",
                    required = true,
                    example = "profile-id",
                    schema = @Schema(type = SchemaType.STRING))
            @PathParam("profile_id") String profileId,
            @Parameter(name = "date", in = QUERY, description = "Target date to retrieve a historic version of the profile.") @QueryParam("date")
            @Valid @CheckDateFormat(pattern = "yyyy-mm-dd", message = "Valid date format is yyyy-mm-dd.") String date) {

        var response = profileService.listSpecificMetricProfiles(id, profileId, date);

        return Response.ok().entity(response).build();
    }

    @Tag(name = "Metric Profiles")
    @Operation(
            summary = "List all metric profiles.",
            description = "List all metric profiles.")
    @APIResponse(
            responseCode = "200",
            description = "List of metric profiles.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = MetricProfileResponse.class)))
    @APIResponse(
            responseCode = "401",
            description = "User has not been authenticated.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "403",
            description = "Not permitted.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "404",
            description = "Tenant not found.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "500",
            description = "Internal Server Error.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @SecurityRequirement(name = "Authentication")
    @GET
    @Path("/{id}/metric-profiles")
    @Produces(MediaType.APPLICATION_JSON)
    @CheckEntitlements(role = "admin", resolvers = {
            @Resolver( idResolver = TenantNameResolver.class, pathId = "id")
    })
    @Authenticated
    public Response listAllMetricProfiles(
            @Parameter(description = "The ID of the tenant.",
                    required = true,
                    example = "c242e43f-9869-4fb0-b881-631bc5746ec0",
                    schema = @Schema(type = SchemaType.STRING))
            @PathParam("id")
            @Valid @NotFoundEntity(repository = TenantRepository.class, message = "There is no Tenant with the following id: ") String id,
            @Parameter(name = "date", in = QUERY, description = "Target date to retrieve a historic version of the profile.") @QueryParam("date")
            @Valid @CheckDateFormat(pattern = "yyyy-mm-dd", message = "Valid date format is yyyy-mm-dd.") String date) {

        var response = profileService.listAllMetricProfiles(id, date);

        return Response.ok().entity(response).build();
    }

    @Tag(name = "Operations Profiles")
    @Operation(
            summary = "Get a specific operations profile.",
            description = "List one specific operations profile targeted by it's unique id.")
    @APIResponse(
            responseCode = "200",
            description = "Operations profile found.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = OperationProfileResponse.class)))
    @APIResponse(
            responseCode = "401",
            description = "User has not been authenticated.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "403",
            description = "Not permitted.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "404",
            description = "Operations profile not found.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "500",
            description = "Internal Server Error.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @SecurityRequirement(name = "Authentication")
    @GET
    @Path("/{id}/operations-profiles/{profile_id}")
    @Produces(MediaType.APPLICATION_JSON)
    @CheckEntitlements(role = "admin", resolvers = {
            @Resolver( idResolver = TenantNameResolver.class, pathId = "id")
    })
    @Authenticated
    public Response listSpecificOperationsProfiles(
            @Parameter(description = "The ID of the tenant.",
                    required = true,
                    example = "c242e43f-9869-4fb0-b881-631bc5746ec0",
                    schema = @Schema(type = SchemaType.STRING))
            @PathParam("id")
            @Valid @NotFoundEntity(repository = TenantRepository.class, message = "There is no Tenant with the following id: ") String id,
            @Parameter(description = "The operations profile id.",
                    required = true,
                    example = "profile-id",
                    schema = @Schema(type = SchemaType.STRING))
            @PathParam("profile_id") String profileId,
            @Parameter(name = "date", in = QUERY, description = "Target date to retrieve a historic version of the profile.") @QueryParam("date")
            @Valid @CheckDateFormat(pattern = "yyyy-mm-dd", message = "Valid date format is yyyy-mm-dd.") String date) {

        var response = profileService.listSpecificOperationsProfiles(id, profileId, date);

        return Response.ok().entity(response).build();
    }

    @Tag(name = "Operations Profiles")
    @Operation(
            summary = "List all operations profiles.",
            description = "List all operations profiles.")
    @APIResponse(
            responseCode = "200",
            description = "List of operations profiles.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = OperationProfileResponse.class)))
    @APIResponse(
            responseCode = "401",
            description = "User has not been authenticated.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "403",
            description = "Not permitted.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "404",
            description = "Tenant not found.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "500",
            description = "Internal Server Error.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @SecurityRequirement(name = "Authentication")
    @GET
    @Path("/{id}/operations-profiles")
    @Produces(MediaType.APPLICATION_JSON)
    @CheckEntitlements(role = "admin", resolvers = {
            @Resolver( idResolver = TenantNameResolver.class, pathId = "id")
    })
    @Authenticated
    public Response listAllOperationsProfiles(
            @Parameter(description = "The ID of the tenant.",
                    required = true,
                    example = "c242e43f-9869-4fb0-b881-631bc5746ec0",
                    schema = @Schema(type = SchemaType.STRING))
            @PathParam("id")
            @Valid @NotFoundEntity(repository = TenantRepository.class, message = "There is no Tenant with the following id: ") String id,
            @Parameter(name = "date", in = QUERY, description = "Target date to retrieve a historic version of the profile.") @QueryParam("date")
            @Valid @CheckDateFormat(pattern = "yyyy-mm-dd", message = "Valid date format is yyyy-mm-dd.") String date) {

        var response = profileService.listAllOperationsProfiles(id, date);

        return Response.ok().entity(response).build();
    }



    @Tag(name = "Tenant")
    @Operation(summary = "Fetch Tenant' s report By Report ID",
            description = "Retrieves the reportwith the specific Report ID, for a tenant with specific Tenant ID,  from the ARGO Web API.")
    @APIResponse(
            responseCode = "200",
            description = "The report retrieved",
            content = @Content(schema = @Schema(
                    type = SchemaType.ARRAY,
                    implementation = FullReportResponseDto.class)))
    @APIResponse(
            responseCode = "401",
            description = "User has not been authenticated.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "403",
            description = "Not permitted.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "409",
            description = "Assessment already exists.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "500",
            description = "Internal Server Error.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "501",
            description = "Not Implemented.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @SecurityRequirement(name = "Authentication")
    @GET
    @Path("/{id}/reports/{report-id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)

    @CheckEntitlements(role = "admin", resolvers = {
            @Resolver( idResolver = TenantNameResolver.class, pathId = "id")
    })
    public Response fetchReportByID(@Parameter(
            description = "The ID of the tenant to retrieve report.",
            required = true,
            example = "c242e43f-9869-4fb0-b881-631bc5746ec0",
            schema = @Schema(type = SchemaType.STRING)) @PathParam("id")
                                    @Valid  @NotFoundEntity(repository = TenantRepository.class, message = "There is no Tenant with the following id: ") String id,@Parameter(
            description = "The ID of the report to retrieve.",
            required = true,
            example = "b442e43f-9869-4fb0-b881-631bc5746ec0",
            schema = @Schema(type = SchemaType.STRING)) @PathParam("report-id")
                                    @Valid  String reportId) {

        var reports = reportService.fetchReportById(id,reportId);

        return Response.ok(reports).build();
    }
    public static class PageableTenants extends PageResource<TenantResponseDto> {

        private List<TenantResponseDto> content;

        @Override
        public List<TenantResponseDto> getContent() {
            return content;
        }

        @Override
        public void setContent(List<TenantResponseDto> content) {
            this.content = content;
        }
    }

    public static class PageableTenantMembers extends PageResource<GroupUser> {

        private List<GroupUser> content;

        @Override
        public List<GroupUser> getContent() {
            return content;
        }

        @Override
        public void setContent(List<GroupUser> content) {
            this.content = content;
        }
    }

    public static class PageableTenantInvitations extends PageResource<TenantInvitationResponse> {

        private List<TenantInvitationResponse> content;

        @Override
        public List<TenantInvitationResponse> getContent() {
            return content;
        }

        @Override
        public void setContent(List<TenantInvitationResponse> content) {
            this.content = content;
        }
    }
}
