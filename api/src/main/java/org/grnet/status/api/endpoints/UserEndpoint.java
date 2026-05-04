package org.grnet.status.api.endpoints;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.grnet.endpoint.scanner.runtime.SecuredEndpoint;
import org.grnet.status.dtos.InformativeResponse;
import org.grnet.status.dtos.user.UserProfileDto;
import org.grnet.status.services.GroupManagementService;
import org.grnet.status.services.StatusPageService;
import org.grnet.status.services.UserService;
import org.grnet.status.util.Utility;

import static org.eclipse.microprofile.openapi.annotations.enums.ParameterIn.QUERY;

@Path("/v1/users")
@Authenticated
@SecurityScheme(securitySchemeName = "Authentication",
        description = "JWT token",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER)
public class UserEndpoint {

    @Inject
    UserService userService;

    @Inject
    GroupManagementService groupManagementService;

    @Inject
    StatusPageService statusPageService;

    @Inject
    Utility utility;

    @Tag(name = "User")
    @Operation(
            summary = "Get User Profile.",
            description = "This endpoint retrieves the user profile information.")
    @APIResponse(
            responseCode = "200",
            description = "User's Profile.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = UserProfileDto.class)))
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
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/profile")
    @SecuredEndpoint(bypass = true)
    public Response profile() {

        var response = userService.getUserProfile(utility.getUserUniqueIdentifier());

        return Response.ok(response).build();
    }

    @Tag(name = "User")
    @Operation(
            summary = "Register as member",
            description = "Registers the authenticated user as a platform member. This is a self-registration operation."
    )
    @APIResponse(
            responseCode = "200",
            description = "User successfully registered as a member.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "204",
            description = "User has already registered as a member.",
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
    @POST
    @Path("/registration")
    @Produces(MediaType.APPLICATION_JSON)

    @SecuredEndpoint(bypass = true)
    public Response registerMember() {

        groupManagementService.addMember("members", utility.getUsername());

        var response = new InformativeResponse();
        response.code = 200;
        response.message = "Registration completed.";

        return Response.ok(response).build();
    }

    @Tag(name = "User")
    @Operation(
            summary = "Get all Status Pages for user",
            description = "Registers the authenticated user as a platform member. This is a self-registration operation."
    )
    @APIResponse(
            responseCode = "200",
            description = "User successfully registered as a member.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "204",
            description = "User has already registered as a member.",
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
    @Path("/pages")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllStatusPagesForUser(
            @Parameter(name = "search", in = QUERY,
                    description = "The \"search\" parameter is a query parameter that allows clients to specify a text string that will be used to search for matches in specific fields in Project entity. " +
                            "The search will be conducted in the following fields : projects' name. ")
            @QueryParam("search") String search,
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

        var statusPages = statusPageService.getAccessibleStatusPages(utility.getUserUniqueIdentifier(), search,page - 1, size, uriInfo);

        return Response.ok(statusPages).build();
    }
}


