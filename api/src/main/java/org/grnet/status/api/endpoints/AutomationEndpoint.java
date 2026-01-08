package org.grnet.status.api.endpoints;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeIn;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.grnet.status.authorizations.interceptors.CheckEntitlements;
import org.grnet.status.constraints.NotFoundEntity;
import org.grnet.status.dtos.InformativeResponse;
import org.grnet.status.dtos.tenant.status.TenantStatusDto;
import org.grnet.status.repositories.TenantRepository;
import org.grnet.status.services.TenantService;

import java.io.IOException;

@Path("/v1/automation")
@Authenticated
@SecurityScheme(securitySchemeName = "Authentication",
        description = "JWT token",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER)
@CheckEntitlements(group = "automation")
public class AutomationEndpoint {

    @Inject
    TenantService tenantService;


    @Tag(name = "Automation")
    @Operation(
            summary = "Update status in tenant.",
            description = "This endpoint returns an object representing the status of the tenant")
    @APIResponse(
            responseCode = "200",
            description = "Status of the existing tenant.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = TenantStatusDto.class)))
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
    @PUT
    @Path("/tenant/{id}/status")
    @CheckEntitlements(role = "admin")
    public Response updateStatus(@PathParam("id")
                                 @Valid @NotFoundEntity(repository = TenantRepository.class, message = "There is no Tenant with the following id: ") String id,
                                 TenantStatusDto request) throws IOException {

        var status = tenantService.updateTenantStatus(id, request);
        return Response.ok().entity(status).build();
    }
}
