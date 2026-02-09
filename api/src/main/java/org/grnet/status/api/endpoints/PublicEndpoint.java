package org.grnet.status.api.endpoints;

import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.grnet.status.dtos.InformativeResponse;
import org.grnet.status.dtos.statuspage.StatusPageConfigDto;
import org.grnet.status.services.StatusService;

@Tag(name = "Public")
@Path("/v1/public")
public class PublicEndpoint {

    @Inject
    StatusService statusService;

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
    @Path("/pages/{slug}")
    @Produces(MediaType.APPLICATION_JSON)
    @PermitAll
    public Response getStatusPageConfig(@PathParam("slug") String slug) {

        var statusPage = statusService.getConfigBySlug(slug);

        return Response.ok(statusPage).build();
    }

}
