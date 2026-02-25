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
import org.grnet.status.api.resolvers.TenantNameResolver;
import org.grnet.status.authorizations.dtos.GroupUserResponse;
import org.grnet.status.authorizations.dtos.MemberRequest;
import org.grnet.status.authorizations.dtos.PartialGroup;
import org.grnet.status.authorizations.interceptors.CheckEntitlements;
import org.grnet.status.authorizations.interceptors.Resolver;
import org.grnet.status.constraints.NotFoundEntity;
import org.grnet.status.dtos.InformativeResponse;
import org.grnet.status.dtos.pagination.PageResource;
import org.grnet.status.dtos.project.ProjectRequestDto;
import org.grnet.status.dtos.project.ProjectResponseDto;
import org.grnet.status.dtos.project.ProjectUpdateDto;
import org.grnet.status.dtos.statuspage.StatusPageResponseDto;
import org.grnet.status.dtos.tenant.ContactFullDto;
import org.grnet.status.dtos.tenant.TenantRequestDto;
import org.grnet.status.dtos.tenant.TenantResponseDto;
import org.grnet.status.dtos.tenant.invitations.TenantInvitationResponse;
import org.grnet.status.dtos.tenantproject.TenantProjectDeleteDto;
import org.grnet.status.dtos.tenantproject.TenantProjectRequestDto;
import org.grnet.status.dtos.tenantproject.TenantProjectDto;
import org.grnet.status.enums.TenantGroupStatus;
import org.grnet.status.dtos.tenant.alerts.AlertDefinitionRequest;
import org.grnet.status.dtos.tenant.status.TenantStatusDto;
import org.grnet.status.dtos.tenant.status.TenantStatusFullResponse;
import org.grnet.status.repositories.ProjectRepository;
import org.grnet.status.repositories.TenantRepository;
import org.grnet.status.services.*;
import org.grnet.status.util.Utility;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.eclipse.microprofile.openapi.annotations.enums.ParameterIn.QUERY;

@Path("/v1/admin")
@Authenticated
@SecurityScheme(
        securitySchemeName = "Authentication",
        description = "JWT token",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER)
@CheckEntitlements
public class AdminEndpoint {


    @Inject
    TenantService tenantService;

    @Inject
    ProjectService projectService;

    @Inject
    TenantProjectService tenantProjectService;

    @Inject
    Utility utility;

    @Inject
    ContactService contactService;

    @Inject
    TenantInvitationService tenantInvitationService;

    @Inject
    GroupManagementService groupManagementService;


    // --------------------------------------------------------------------------------------------------------------------------
    // ADMIN TENANT ENDPOINT
    // --------------------------------------------------------------------------------------------------------------------------

    @Tag(name = "Admin")
    @Operation(summary = "Create Tenant",
            description = "Creates a tenant to service and web-api")
    @APIResponse(
            responseCode = "200",
            description = "Secret encrypted successfully",
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
            responseCode = "409",
            description = "Tenant already exists.",
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
    @POST
    @Path("/tenants")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(@Valid TenantRequestDto request) throws IOException {

        var response = tenantService.create(request, utility.getUserUniqueIdentifier());
        return Response.ok(response).build();
    }



    @Tag(name = "Admin")
    @Operation(
            summary = "Delete Tenant By Id .",
            description = "Deletes a specific tenant assessment.")
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
    @DELETE
    @Path("/tenants/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteTenant(@Parameter(
            description = "The ID of the tenant to be deleted.",
            required = true,
            example = "c242e43f-9869-4fb0-b881-631bc5746ec0",
            schema = @Schema(type = SchemaType.STRING)) @PathParam("id")
                                 @Valid @NotFoundEntity(repository = TenantRepository.class, message = "There is no Tenant with the following id: ") String id) {

        tenantService.deleteTenantById(id);

        var informativeResponse = new InformativeResponse();
        informativeResponse.code = 200;
        informativeResponse.message = "Tenant has been successfully deleted.";
        return Response.ok().entity(informativeResponse).build();
    }

    @Tag(name = "Admin")
    @Operation(
            summary = "Create AGM group for tenant.",
            description = "Returns a specific tenant assessment.")
    @APIResponse(
            responseCode = "200",
            description = "Group already exist.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = TenantGroupStatus.class)))
    @APIResponse(
            responseCode = "201",
            description = "Group created.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = TenantGroupStatus.class)))
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
    @POST
    @Path("/tenants/{id}/group")
    @Produces(MediaType.APPLICATION_JSON)
    @Authenticated
    public Response createGroupTenant(
            @Parameter(description = "The ID of the tenant to retrieve.",
                    required = true,
                    example = "c242e43f-9869-4fb0-b881-631bc5746ec0",
                    schema = @Schema(type = SchemaType.STRING))
            @PathParam("id")
            @Valid @NotFoundEntity(repository = TenantRepository.class, message = "There is no Tenant with the following id: ") String id) {

        var status = tenantService.createTenantGroup(id);

        return Response.ok().entity(Map.of("group-status", status)).build();
    }


    @Tag(name = "Admin")
    @Operation(
            summary = "Get list of tenants.",
            description = "This endpoint returns a list of tenants " +
                    "By default, the first page of 10 tenant objects will be returned. You can tune the default values by using the query parameters page and size.")
    @APIResponse(
            responseCode = "200",
            description = "List of tenant objects existing.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = PageableTenants.class)))
    @APIResponse(
            responseCode = "400",
            description = "Bad Request",
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
    @GET
    @Path("/tenants")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTenantsByPageAndSize(
            @Parameter(name = "page", in = QUERY,
                    description = "Indicates the page number. Page number must be >= 1.")
            @DefaultValue("1") @Min(value = 1, message = "Page number must be >= 1.")
            @QueryParam("page")
            int page,
            @Parameter(name = "size", in = QUERY,
                    description = "The page size.")
            @DefaultValue("10") @Min(value = 1, message = "Page size must be between 1 and 100.") @Max(value = 100, message = "Page size must be between 1 and 100.")
            @QueryParam("size")
            int size,
            @Parameter(name = "search", in = QUERY,
                    description = "The \"search\" parameter is a query parameter that allows clients to specify a text string that will be used to search for matches in specific fields in Tenant entity. The search will be conducted in the following fields : tenants' name, tenant's email.") @QueryParam("search") String search,
            @Parameter(name = "sort", in = QUERY,
                    schema = @Schema(type = SchemaType.STRING, defaultValue = ""),
                    examples = {@ExampleObject(name = "Tenant name", value = "name"), @ExampleObject(name = "Created At", value = "createdAt")},
                    description = "The \"sort\" parameter allows clients to specify the field by which they want the results to be sorted.") @DefaultValue("createdAt") @QueryParam("sort") String sort,
            @Parameter(name = "order",
                    in = QUERY,
                    schema = @Schema(type = SchemaType.STRING, defaultValue = ""),
                    examples = {@ExampleObject(name = "Ascending", value = "ASC"), @ExampleObject(name = "Descending", value = "DESC")},
                    description = "The \"order\" parameter specifies the order in which the sorted results should be returned.") @DefaultValue("DESC") @QueryParam("order") String order,
            @Context UriInfo uriInfo) {
        var orderValues = List.of("ASC", "DESC");
        var sortValues = List.of("name", "createdAt");

        if (!orderValues.contains(order)) {

            throw new BadRequestException("The available values of order parameter are : " + orderValues);
        }

        if (!sortValues.contains(sort)) {

            throw new BadRequestException("The available values of sort parameter are : " + sortValues);
        }

        var assessments = tenantService.getTenantsByPageAndSize(page - 1, size, uriInfo, search, sort, order);

        return Response.ok().entity(assessments).build();
    }

    // --------------------------------------------------------------------------------------------------------------------------
    // ADMIN PROJECT ENDPOINT
    // --------------------------------------------------------------------------------------------------------------------------

    @Tag(name = "Admin")
    @Operation(summary = "Create a project",
            description = "Create a new project entry.")
    @APIResponse(
            responseCode = "201",
            description = "Project created",
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
            responseCode = "409",
            description = "Project already exists.",
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
    @POST
    @Path("/projects")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createProject(
            @Valid @NotNull(message = "The request body is empty.")
            ProjectRequestDto request,
            @Context UriInfo uriInfo) {

        var response = projectService.createProject(request);

        return Response.created(uriInfo.getAbsolutePathBuilder().path(response.id).build()).entity(response).build();
    }


    @Tag(name = "Admin")
    @Operation(summary = "Get a project",
            description = "Returns a specific project.")
    @APIResponse(
            responseCode = "200",
            description = "Get project ",
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
    @Path("/projects/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProject(
            @Parameter(
                    description = "The ID of the project to retrieve.",
                    required = true,
                    example = "proj-32262f66f6e1",
                    schema = @Schema(type = SchemaType.STRING))
            @PathParam("id")
            @Valid @NotFoundEntity(repository = ProjectRepository.class, message = "There is no Project with the following id: ") String id) {

        var project = projectService.getProjectById(id);

        return Response.ok().entity(project).build();
    }

    @Tag(name = "Admin")
    @Operation(summary = "Update a project",
            description = "Updates a specific project.")
    @APIResponse(
            responseCode = "200",
            description = "Project updated",
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
            responseCode = "409",
            description = "Project already exists.",
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
    @PUT
    @Path("/projects/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateProject(
            @Parameter(
                    description = "The ID of the project to retrieve.",
                    required = true,
                    example = "proj-32262f66f6e1",
                    schema = @Schema(type = SchemaType.STRING))
            @PathParam("id")
            @Valid @NotFoundEntity(repository = ProjectRepository.class, message = "There is no Project with the following id: ") String id,
            @Valid @NotNull(message = "The request body is empty.")
            ProjectUpdateDto request) {

        var project = projectService.updateProjectById(id, request);

        return Response.ok().entity(project).build();
    }

    @Tag(name = "Admin")
    @Operation(summary = "Delete a project",
            description = "Deletes a specific project.")
    @APIResponse(
            responseCode = "200",
            description = "Delete project ",
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
    @DELETE
    @Path("/projects/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteProject(
            @Parameter(
                    description = "The ID of the project to delete.",
                    required = true,
                    example = "proj-32262f66f6e1",
                    schema = @Schema(type = SchemaType.STRING))
            @PathParam("id")
            @Valid @NotFoundEntity(repository = ProjectRepository.class, message = "There is no Project with the following id: ") String id) {

        projectService.deleteById(id);

        var informativeResponse = new InformativeResponse();
        informativeResponse.code = 200;
        informativeResponse.message = "Project has been successfully deleted.";

        return Response.ok().entity(informativeResponse).build();
    }

    @Tag(name = "Admin")
    @Operation(
            summary = "List all projects",
            description = "Retrieve a list of all projects."
    )
    @APIResponse(
            responseCode = "200",
            description = "List of projects",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = PageableProject.class)))
    @APIResponse(
            responseCode = "401",
            description = "User not authenticated.",
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
            responseCode = "500",
            description = "Internal Server Error.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @SecurityRequirement(name = "Authentication")
    @GET
    @Path("/projects")
    @Produces(MediaType.APPLICATION_JSON)
    public Response fetchAllProjects(
            @Parameter(name = "Search", in = QUERY,
                    description = "Search term applied on the Project. ")
            @QueryParam("search") String search,
            @Parameter(name = "sort", in = QUERY,
                    schema = @Schema(type = SchemaType.STRING, defaultValue = "startDate"),
                    examples = {
                            @ExampleObject(name = "Start Date", value = "startDate"),
                            @ExampleObject(name = "End Date", value = "endDate"),
                            @ExampleObject(name = "Name", value = "name"),
                            @ExampleObject(name = "Created At", value = "createdAt"),
                            @ExampleObject(name = "Updated At", value = "updatedAt")
                    },
                    description = "Field used for sorting the project list.")
            @DefaultValue("startDate")
            @QueryParam("sort") String sort,
            @Parameter(name = "Order", in = QUERY,
                    schema = @Schema(type = SchemaType.STRING, defaultValue = "DESC"),
                    examples = {
                            @ExampleObject(name = "Ascending", value = "ASC"),
                            @ExampleObject(name = "Descending", value = "DESC")},
                    description = "The \"order\" parameter specifies the order in which the sorted results should be returned.")
            @DefaultValue("DESC")
            @QueryParam("order") String order,
            @Parameter(name = "page", in = QUERY,
                    description = "Indicates the page number. Page number must be >= 1.")
            @DefaultValue("1")
            @Min(value = 1, message = "Page number must be >= 1.")
            @QueryParam("page") int page,
            @Parameter(name = "size", in = QUERY,
                    description = "The page size.")
            @DefaultValue("10")
            @Min(value = 1, message = "Page size must be between 1 and 100.")
            @Max(value = 100, message = "Page size must be between 1 and 100.")
            @QueryParam("size") int size,
            @Context UriInfo uriInfo) {

        var responseList = projectService.getAllProjectsByPageAndSize(page - 1, size, search, sort, order, uriInfo);

        return Response.ok(responseList).build();
    }


    @Tag(name = "Admin")
    @Operation(summary = "List the tenants of a project",
            description = "Retrieves a list of tenants of a project")
    @APIResponse(
            responseCode = "200",
            description = "Tenants list retrieved",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = PageableProject.class)))
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
    @Path("/projects/{id}/tenants")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTenantsByProject(
            @Parameter(
                    description = "The ID of the project to retrieve.",
                    required = true,
                    example = "proj-32262f66f6e1",
                    schema = @Schema(type = SchemaType.STRING))
            @PathParam("id")
            @Valid @NotFoundEntity(repository = ProjectRepository.class, message = "There is no Project with the following id: ") String id,
            @Parameter(name = "search", in = QUERY,
                    description = "The \"search\" parameter is a query parameter that allows clients to specify a text string that will be used to search for matches in specific fields in Tenant entity. " +
                            "The search will be conducted in the following fields : tenants' name, tenant's email.")
            @QueryParam("search") String search,
            @Parameter(name = "sort", in = QUERY,
                    schema = @Schema(type = SchemaType.STRING, defaultValue = ""),
                    examples = {
                            @ExampleObject(name = "Tenant name", value = "name"),
                            @ExampleObject(name = "Created At", value = "createdAt")},
                    description = "The \"sort\" parameter allows clients to specify the field by which they want the results to be sorted.")
            @DefaultValue("createdAt")
            @QueryParam("sort")
            String sort,
            @Parameter(name = "order", in = QUERY,
                    schema = @Schema(type = SchemaType.STRING, defaultValue = ""),
                    examples = {
                            @ExampleObject(name = "Ascending", value = "ASC"),
                            @ExampleObject(name = "Descending", value = "DESC")},
                    description = "The \"order\" parameter specifies the order in which the sorted results should be returned.")
            @DefaultValue("DESC")
            @QueryParam("order")
            String order,
            @Parameter(name = "page", in = QUERY,
                    description = "Indicates the page number. Page number must be >= 1.")
            @DefaultValue("1") @Min(value = 1, message = "Page number must be >= 1.")
            @QueryParam("page")
            int page,
            @Parameter(name = "size", in = QUERY,
                    description = "The page size.")
            @DefaultValue("10") @Min(value = 1, message = "Page size must be between 1 and 100.") @Max(value = 100, message = "Page size must be between 1 and 100.")
            @QueryParam("size")
            int size,
            @Context UriInfo uriInfo) {

        var allowedSort = List.of("name", "email", "createdAt");
        var allowedOrder = List.of("ASC", "DESC");

        if (!allowedSort.contains(sort)) {
            throw new BadRequestException("The available values of sort parameter are : " + allowedSort);
        }

        if (!allowedOrder.contains(order)) {
            throw new BadRequestException("The available values of order parameter are : " + allowedOrder);
        }

        var project = tenantProjectService.getTenantsByProject(id, page - 1, size, uriInfo, search, sort, order);

        return Response.ok().entity(project).build();
    }


    @Tag(name = "Admin")
    @Operation(
            summary = "Manage tenant–project assignments",
            description = "Assign, unassign, or update multiple project assignments for a tenant.")
    @APIResponse(
            responseCode = "200",
            description = "Assignments created or already existing.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "400",
            description = "Invalid request.",
            content = @Content(schema = @Schema(
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "401",
            description = "User not authenticated.",
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
            description = "Tenant or Project not found.",
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
    @PUT
    @Path("/tenant-project")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response assignProjectToTenants(
            @Valid @NotNull TenantProjectRequestDto request) {

        var result = tenantProjectService.assign(request);
        return Response.ok(result).build();
    }


    // --------------------------------------------------------------------------------------------------------------------------
    // ADMIN HELPER METHODS
    // --------------------------------------------------------------------------------------------------------------------------
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

    public static class PageableTenantProject extends PageResource<TenantProjectDto> {

        private List<TenantProjectDto> content;

        @Override
        public List<TenantProjectDto> getContent() {
            return content;
        }

        @Override
        public void setContent(List<TenantProjectDto> content) {
            this.content = content;
        }
    }

    @Tag(name = "Admin")
    @Operation(
            summary = "Get list of contacts.",
            description = "This endpoint returns a list of contacts " +
                    "By default, the first page of 10 contact objects will be returned. You can tune the default values by using the query parameters page and size.")
    @APIResponse(
            responseCode = "200",
            description = "List of tenant objects existing.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = PageableContacts.class)))
    @APIResponse(
            responseCode = "400",
            description = "Bad Request",
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
    @GET
    @Path("/contacts")
    @Produces(MediaType.APPLICATION_JSON)
    @Authenticated
    public Response getContactsByPageAndSize(
            @Parameter(name = "page", in = QUERY,
                    description = "Indicates the page number. Page number must be >= 1.")
            @DefaultValue("1") @Min(value = 1, message = "Page number must be >= 1.")
            @QueryParam("page")
            int page,
            @Parameter(name = "size", in = QUERY,
                    description = "The page size.")
            @DefaultValue("10") @Min(value = 1, message = "Page size must be between 1 and 100.") @Max(value = 100, message = "Page size must be between 1 and 100.")
            @QueryParam("size")
            int size,
            @Context UriInfo uriInfo) {

        var contacts = contactService.getContactsByPageAndSize(page - 1, size, uriInfo);

        return Response.ok().entity(contacts).build();
    }

    @Tag(name = "Admin")
    @Operation(summary = "Get list of groups.",
            description = "Returns the groups of the Status Page.")
    @APIResponse(
            responseCode = "200",
            description = "Get groups.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = PageableGroups.class)))
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
            responseCode = "500",
            description = "Internal Server Error.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @SecurityRequirement(name = "Authentication")
    @GET
    @Path("/groups")
    @Produces(MediaType.APPLICATION_JSON)
    public Response fetchGroups(@Parameter(name = "page", in = QUERY,
                                        description = "Indicates the page number. Page number must be >= 1.")
                                @DefaultValue("1") @Min(value = 1, message = "Page number must be >= 1.")
                                @QueryParam("page")
                                int page,
                                @Parameter(name = "size", in = QUERY,
                                        description = "The page size.")
                                @DefaultValue("10") @Min(value = 1, message = "Page size must be between 1 and 100.") @Max(value = 100, message = "Page size must be between 1 and 100.")
                                @QueryParam("size")
                                int size,
                                @Context UriInfo uriInfo) {

        var project = groupManagementService.fetchGroups(page - 1, size, uriInfo);

        return Response.ok().entity(project).build();
    }

    @Tag(name = "Admin")
    @Operation(summary = "Get list of members",
            description = "Returns the members of the Status Page")
    @APIResponse(
            responseCode = "200",
            description = "Get members ",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = PageableGroupUserResponse.class)))
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
            description = "Group does not exist.",
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
    @Path("/members")
    @Produces(MediaType.APPLICATION_JSON)
    public Response fetchMembers(
            @Parameter(name = "search", in = QUERY,
                    description = "Search term applied on members (user id, username, email, first name, last name).")
            @QueryParam("search") String search,
            @Parameter(name = "page", in = QUERY,
                    description = "Indicates the page number. Page number must be >= 1.")
            @DefaultValue("1")
            @Min(value = 1, message = "Page number must be >= 1.")
            @QueryParam("page") int page,

            @Parameter(name = "size", in = QUERY,
                    description = "The page size.")
            @DefaultValue("10")
            @Min(value = 1, message = "Page size must be between 1 and 100.")
            @Max(value = 100, message = "Page size must be between 1 and 100.")
            @QueryParam("size") int size,

            @Context UriInfo uriInfo) {

        var project = groupManagementService.getAllMembers("members", search, page - 1, size, uriInfo);

        return Response.ok().entity(project).build();
    }

    @Tag(name = "Admin")
    @Operation(summary = "Notify AMS to initialize the automation process of an event for a tenant",
            description = "Notify AMS to initialize automation process.")
    @APIResponse(
            responseCode = "201",
            description = "Process initialized",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = TenantStatusDto.class)))
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
            description = "Project already exists.",
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
    @POST
    @Path("/tenants/{id}/notify-ams")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response notifyAms(
            @Parameter(
                    description = "The ID of the tenant to start automation process.",
                    required = true,
                    example = "c242e43f-9869-4fb0-b881-631bc5746ec0",
                    schema = @Schema(type = SchemaType.STRING)) @PathParam("id")
            @Valid @NotFoundEntity(repository = TenantRepository.class, message = "There is no Tenant with the following id: ") String id,
            @Valid @NotNull(message = "The request body is empty.")
            AlertDefinitionRequest request) {

        var status = tenantService.notifyAms(id, request);
        return Response.ok().entity(status).build();
    }

    @Tag(name = "Admin")
    @Operation(
            summary = "Update Tenant's status By Id .",
            description = "Returns a specific tenant's status.")
    @APIResponse(
            responseCode = "200",
            description = "The corresponding tenant's status.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = TenantStatusFullResponse.class)))
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
    @PATCH
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/tenants/{id}/manual/status")
    public Response updateStatus(
            @PathParam("id")
            @Valid @NotFoundEntity(repository = TenantRepository.class, message = "There is no Tenant with the following id: ")
            String id, TenantStatusDto request) {

        var status = tenantService.updateTenantManualJobs(id, request);
        return Response.ok().entity(status).build();
    }


    @Tag(name = "Admin")
    @Operation(
            summary = "Get all invitations.",
            description = "Returns all invitation for super admin. "
    )
    @APIResponse(
            responseCode = "200",
            description = "Invitation details.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = PageableInvitations.class)))
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
    @SecurityRequirement(name = "Authentication")
    @GET
    @Path("/invitations")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getInvitation(

            @Parameter(name = "search", in = QUERY,
                    description = "Search invitations by Tenant Name or user email.")
            @QueryParam("search") String search,
            @Parameter(name = "sort", in = QUERY,
                    schema = @Schema(type = SchemaType.STRING, defaultValue = "createdAt"),
                    examples = {
                            @ExampleObject(name = "Created At", value = "createdAt"),
                            @ExampleObject(name = "Email", value = "email"),
                            @ExampleObject(name = "Tenant Name", value = "tenantName"),
                            @ExampleObject(name = "Status", value = "status"),
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
            int size, @Context UriInfo uriInfo) {
        var response = tenantInvitationService.getInvitationsByPageAndSize(search, sort, order, page - 1, size, uriInfo);

        return Response.ok(response).build();
    }

    @Tag(name = "Admin")
    @Operation(
            summary = "Add member to tenant group.",
            description = "Add member to tenant group.")
    @APIResponse(
            responseCode = "200",
            description = "Member added to tenant group.",
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
    @POST
    @Path("/tenants/{id}/members")
    @Produces(MediaType.APPLICATION_JSON)
    @Authenticated
    public Response addMemberToGroup(
            @Parameter(description = "The ID of the tenant.",
                    required = true,
                    example = "c242e43f-9869-4fb0-b881-631bc5746ec0",
                    schema = @Schema(type = SchemaType.STRING))
            @PathParam("id")
            @Valid @NotFoundEntity(repository = TenantRepository.class, message = "There is no Tenant with the following id: ") String id,
            @Valid @NotNull(message = "The request body is empty.") MemberRequest request) {

        groupManagementService.addMemberToGroup(id, request.username, request.role, request.email);

        var response = new InformativeResponse();
        response.code = 200;
        response.message = "Member added successfully to group";

        return Response.ok().entity(response).build();
    }

    public static class PageableGroupUserResponse extends PageResource<GroupUserResponse> {

        private List<GroupUserResponse> content;

        @Override
        public List<GroupUserResponse> getContent() {
            return content;
        }

        @Override
        public void setContent(List<GroupUserResponse> content) {
            this.content = content;
        }
    }

    public static class PageableInvitations extends PageResource<TenantInvitationResponse> {

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

    public static class PageableGroups extends PageResource<PartialGroup> {

        private List<PartialGroup> content;

        @Override
        public List<PartialGroup> getContent() {
            return content;
        }

        @Override
        public void setContent(List<PartialGroup> content) {
            this.content = content;
        }
    }

    public static class PageableContacts extends PageResource<ContactFullDto> {

        private List<ContactFullDto> content;

        @Override
        public List<ContactFullDto> getContent() {
            return content;
        }

        @Override
        public void setContent(List<ContactFullDto> content) {
            this.content = content;
        }
    }

    public static class PageableStatusPages extends PageResource<StatusPageResponseDto> {

        private List<StatusPageResponseDto> content;

        @Override
        public List<StatusPageResponseDto> getContent() {
            return content;
        }

        @Override
        public void setContent(List<StatusPageResponseDto> content) {
            this.content = content;
        }
    }

    public static class PageableProject extends PageResource<ProjectResponseDto> {

        private List<ProjectResponseDto> content;

        @Override
        public List<ProjectResponseDto> getContent() {
            return content;
        }

        @Override
        public void setContent(List<ProjectResponseDto> content) {
            this.content = content;
        }
    }

}
