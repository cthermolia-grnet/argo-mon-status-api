package org.grnet.status.api.endpoints;

import io.quarkus.security.Authenticated;
import jakarta.annotation.security.PermitAll;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeIn;
import org.eclipse.microprofile.openapi.annotations.enums.SecuritySchemeType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.grnet.status.authorizations.interceptors.CheckEntitlements;
import org.grnet.status.dtos.InformativeResponse;
import org.grnet.status.dtos.status.StatusGroupRequestDto;
import org.grnet.status.dtos.statuspage.StatusPageConfigDto;
import org.grnet.status.services.StatusService;

@Path("/v1/")
@Authenticated
@SecurityScheme(securitySchemeName = "Authentication",
        description = "JWT token",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER)
@CheckEntitlements(group = "tenants")
public class StatusEndpoint {

    @Inject
    StatusService statusService;

    @Tag(name = "Status")
    @Operation(summary = "Fetch status groups for a report",
            description = "Decrypts the provided secret key and retrieves report  groups from the ARGO Web API.")
    @APIResponse(
            responseCode = "200",
            description = "List of available reports",
            content = @Content(schema = @Schema(
                    type = SchemaType.ARRAY,
                    implementation = StatusGroupRequestDto.class))
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
    @POST
    @Path("/status/groups")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response fetchStatusGroups(StatusGroupRequestDto request) {

        var reports = statusService.getStatusGroups(request);

        return Response.ok(reports).build();
    }


    @Tag(name = "Public Status Pages")
    @Operation(
            summary = "Get status page configuration by slug",
            description = "Returns only the public configuration (config field) for the given slug."
    )
    @APIResponse(
            responseCode = "200",
            description = "Configuration found",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = StatusPageConfigDto.class,
                    description = "The stored public configuration object"
            ))
    )
    @APIResponse(
            responseCode = "404",
            description = "Status page not found",
            content = @Content(schema = @Schema(
                    implementation = InformativeResponse.class
            ))
    )
    @APIResponse(
            responseCode = "500",
            description = "Internal Server Error.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class))
    )
    @GET
    @Path("/status/{slug}")
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    public Response getStatusPageConfig(@PathParam("slug") String slug) {

        var statusPage = statusService.getConfigBySlug(slug);

        return Response.ok(statusPage).build();
    }

}
