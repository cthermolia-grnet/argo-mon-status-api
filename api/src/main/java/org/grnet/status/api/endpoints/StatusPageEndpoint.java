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
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.grnet.status.authorizations.interceptors.CheckEntitlements;
import org.grnet.status.constraints.NotFoundEntity;
import org.grnet.status.dtos.InformativeResponse;
import org.grnet.status.dtos.general.ExistResponseDto;
import org.grnet.status.dtos.pagination.PageResource;
import org.grnet.status.dtos.statuspage.StatusPageRequestDto;
import org.grnet.status.dtos.statuspage.StatusPageResponseDto;
import org.grnet.status.dtos.statuspage.StatusPageUpdateRequestDto;
import org.grnet.status.repositories.StatusPageRepository;
import org.grnet.status.repositories.TenantRepository;
import org.grnet.status.services.StatusPageService;
import org.grnet.status.util.Utility;

import java.util.List;

import static org.eclipse.microprofile.openapi.annotations.enums.ParameterIn.QUERY;

@Path("/v1/pages")
public class StatusPageEndpoint {

    @Inject
    StatusPageService statusPageService;

    @Inject
    Utility utility;

    @Tag(name = "Status Pages")
    @Operation(
            summary = "Create a new status page.",
            description = "This endpoint allows an authenticated user to create a new ARGO Status Page."
    )
    @APIResponse(
            responseCode = "201",
            description = "Status Page created successfully.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = StatusPageResponseDto.class))
    )
    @APIResponse(
            responseCode = "400",
            description = "Invalid request payload.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class))
    )
    @APIResponse(
            responseCode = "401",
            description = "User has not been authenticated.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class))
    )
    @APIResponse(
            responseCode = "409",
            description = "Slug already exists.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class))
    )
    @APIResponse(
            responseCode = "500",
            description = "Internal Server Error.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @SecurityRequirement(name = "Authentication")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Authenticated
    public Response createStatusPage(
            @Valid @NotNull(message = "The request body is empty.")
            StatusPageRequestDto request,
            @Context UriInfo uriInfo) {

        var response = statusPageService.createStatusPage(request, utility.getUserUniqueIdentifier());

        return Response.created(uriInfo.getAbsolutePathBuilder().path(response.id).build()).entity(response).build();
    }

    @Tag(name = "Status Pages")
    @Operation(
            summary = "Get a status page by ID.",
            description = "Returns a specific status page."
    )
    @APIResponse(
            responseCode = "200",
            description = "The corresponding status page.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = StatusPageResponseDto.class))
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
            responseCode = "500",
            description = "Internal Server Error.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @SecurityRequirement(name = "Authentication")
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Authenticated
    public Response getStatusPage(
            @Parameter(
                    description = "The ID of the status page to retrieve.",
                    required = true,
                    example = "e7ab046c-8544-47e6-bd8f-e8aa8b83acb0",
                    schema = @Schema(type = SchemaType.STRING))
            @PathParam("id") @Valid @NotFoundEntity(repository = StatusPageRepository.class, message = "There is no Status Page with the following id:") String id) {

        var page = statusPageService.getStatusPageById(id);
        return Response.ok().entity(page).build();
    }

    @Tag(name = "Status Pages")
    @Operation(
            summary = "List status pages with pagination.",
            description = "Returns paginated list of status pages for the authenticated user."
    )
    @APIResponse(
            responseCode = "200",
            description = "List of status pages.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = StatusPageEndpoint.PageableStatusPages.class))
    )
    @APIResponse(
            responseCode = "401",
            description = "User has not been authenticated.",
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
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Authenticated
    public Response listStatusPages(
            @Parameter(name = "page", in = QUERY,
                    description = "Indicates the page number. Page number must be >= 1.")
            @DefaultValue("1") @Min(value = 1, message = "Page number must be >= 1.") @QueryParam("page") int page,
            @Parameter(name = "size", in = QUERY,
                    description = "The page size.")
            @DefaultValue("10") @Min(value = 1, message = "Page size must be between 1 and 100.")
            @Max(value = 100, message = "Page size must be between 1 and 100.") @QueryParam("size") int size,
            @Context UriInfo uriInfo) {

        var pages = statusPageService.getStatusPageByUserAndPage(page - 1, size, uriInfo, utility.getUserUniqueIdentifier());

        return Response.ok().entity(pages).build();
    }

    @Tag(name = "Status Pages")
    @Operation(
            summary = "Update a status page.",
            description = "Updates a specific status page."
    )
    @APIResponse(
            responseCode = "200",
            description = "Page updated successfully.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = StatusPageResponseDto.class))
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
            description = "Slug already exists.",
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
    @Authenticated
    public Response updateStatusPage(
            @Parameter(description = "The ID of the status page to update.",
                    required = true,
                    example = "e7ab046c-8544-47e6-bd8f-e8aa8b83acb0",
                    schema = @Schema(type = SchemaType.STRING)) @PathParam("id") String id,
            @Valid @NotNull(message = "The request body is empty.") StatusPageUpdateRequestDto request) {

        var updated = statusPageService.updateStatusPage(id, request);
        return Response.ok().entity(updated).build();
    }

    @Tag(name = "Status Pages")
    @Operation(
            summary = "Delete a status page.",
            description = "Deletes a specific status page."
    )
    @APIResponse(
            responseCode = "200",
            description = "Deletion completed.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class))
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
            responseCode = "500",
            description = "Internal Server Error.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class))
    )
    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Authenticated
    public Response deleteStatusPage(
            @Parameter(
                    description = "The ID of the status page to delete.",
                    required = true,
                    example = "e7ab046c-8544-47e6-bd8f-e8aa8b83acb0",
                    schema = @Schema(type = SchemaType.STRING))
            @PathParam("id") String id) {

        statusPageService.deleteStatusPage(id);
        var informativeResponse = new InformativeResponse();
        informativeResponse.code = 200;
        informativeResponse.message = "Status Page has been successfully deleted.";
        return Response.ok().entity(informativeResponse).build();
    }

    @Tag(name = "Status Pages")
    @Operation(summary = "Check if a status page slug exists",
            description = "Returns true if a status page with the given slug exists.")
    @APIResponse(
            responseCode = "200",
            description = "Slug existence response",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = ExistResponseDto.class))
    )
    @APIResponse(
            responseCode = "401",
            description = "User has not been authenticated.",
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
    @Path("/check-slug/{slug}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response checkSlugExists(@PathParam("slug") String slug) {

        var exists = statusPageService.slugExists(slug);

        return Response.ok(exists).build();
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

}
