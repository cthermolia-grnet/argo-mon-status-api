package org.grnet.status.api.endpoints;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeIn;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.grnet.endpoint.scanner.runtime.SecuredEndpoint;
import org.grnet.status.constraints.NotFoundEntity;
import org.grnet.status.dtos.InformativeResponse;
import org.grnet.status.dtos.tenant.status.TenantStatusDto;
import org.grnet.status.dtos.tenant.status.TenantStatusFullResponse;
import org.grnet.status.repositories.TenantRepository;
import org.grnet.status.services.TenantService;

@Path("/v1/automation")
@Authenticated
@SecurityScheme(securitySchemeName = "Authentication",
        description = "JWT token",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER)
public class AutomationEndpoint {

    @Inject
    TenantService tenantService;

    @Tag(name = "Automation")
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
    @Path("/tenants/{id}/status")
    @Produces(MediaType.APPLICATION_JSON)
    @SecuredEndpoint
    public Response updateStatus(@PathParam("id")
                                 @Valid @NotFoundEntity(repository = TenantRepository.class, message = "There is no Tenant with the following id: ") String id,
                                 TenantStatusDto request) {

        var status = tenantService.updateTenantAutoJobs(id, request);
        return Response.ok().entity(status).build();
    }

    @Tag(name = "Automation")
    @Operation(
            summary = "Get Tenant's status By Id .",
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

    @GET
    @Path("/tenants/{id}/status")
    @Produces(MediaType.APPLICATION_JSON)
    @SecuredEndpoint
    public Response getTenantStatus(@Parameter(
            description = "The ID of the tenant to retrieve status.",
            required = true,
            example = "c242e43f-9869-4fb0-b881-631bc5746ec0",
            schema = @Schema(type = SchemaType.STRING)) @PathParam("id")
                                    @Valid @NotFoundEntity(repository = TenantRepository.class, message = "There is no Tenant with the following id: ") String id) {

        var status = tenantService.getTenantStatus(id);

        return Response.ok().entity(status).build();
    }
}
