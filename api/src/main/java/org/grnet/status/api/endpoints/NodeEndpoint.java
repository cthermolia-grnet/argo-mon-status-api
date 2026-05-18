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
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.security.SecurityScheme;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.grnet.endpoint.scanner.runtime.SecuredEndpoint;
import org.grnet.status.api.resolvers.CheckDateFormat;
import org.grnet.status.dtos.InformativeResponse;
import org.grnet.status.dtos.tenant.node.*;
import org.grnet.status.services.*;

import static org.eclipse.microprofile.openapi.annotations.enums.ParameterIn.QUERY;

@Path("/v1/nodes")
@Authenticated
@Tag(name = "Node")
@SecurityScheme(
        securitySchemeName = "Authentication",
        description = "JWT token",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER)
public class NodeEndpoint {

    @Inject
    NodeService nodeService;

    @Operation(
            summary = "Get availability results for node services.",
            description = "Retrieve availability metrics for a node’s services from Argo Web API.")
    @APIResponse(
            responseCode = "200",
            description = "Availability retrieved successfully.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = WebApiNodeAvailabilityResponse.class)))
    @APIResponse(
            responseCode = "401",
            description = "User has not been authenticated.",
            content = @Content(schema = @Schema(
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "403",
            description = "Not permitted.",
            content = @Content(schema = @Schema(
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "404",
            description = "Node not found.",
            content = @Content(schema = @Schema(
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "500",
            description = "Internal Server Error.",
            content = @Content(schema = @Schema(
                    implementation = InformativeResponse.class)))
    @SecurityRequirement(name = "Authentication")
    @GET
    @Path("/{name}/capabilities/availability")
    @Produces(MediaType.APPLICATION_JSON)
    @SecuredEndpoint(bypass = true)
    public Response getAvailability(
            @Parameter(description = "The name of the Node.",
                    required = true,
                    example = "GRNET",
                    schema = @Schema(type = SchemaType.STRING))
            @PathParam("name")
            String nodeName,
            @Parameter(name = "item", in = QUERY,
                    description = "Service name to target under the node.",
                    example = "WIKI")
            @QueryParam("item")
            String item,
            @Parameter(name = "date", in = QUERY,
                    description = "Target date (YYYY-MM-DD).")
            @QueryParam("date")
            @Valid
            @CheckDateFormat(pattern = "yyyy-MM-dd", message = "Valid date format is yyyy-MM-dd.")
            String date,
            @Parameter(name = "start_time", in = QUERY,
                    description = "Start time in W3C format.")
            @QueryParam("start_time")
            String startTime,
            @Parameter(name = "end_time", in = QUERY,
                    description = "End time in W3C format.")
            @QueryParam("end_time")
            String endTime,
            @Parameter(name = "start_date", in = QUERY,
                    description = "Start date (YYYY-MM-DD).")
            @QueryParam("start_date")
            @Valid
            @CheckDateFormat(pattern = "yyyy-MM-dd", message = "Valid date format is yyyy-MM-dd.")
            String startDate,
            @Parameter(name = "end_date", in = QUERY,
                    description = "End date (YYYY-MM-DD).")
            @QueryParam("end_date")
            @Valid
            @CheckDateFormat(pattern = "yyyy-MM-dd", message = "Valid date format is yyyy-MM-dd.")
            String endDate,
            @Parameter(name = "granularity", in = QUERY,
                    description = "Granularity of results (daily, monthly).",
                    example = "daily")
            @QueryParam("granularity")
            String granularity
    ) {
        var availability = nodeService.getAvailabilityByNodeName(nodeName, item, date, startTime, endTime, startDate, endDate, granularity);

        return Response.ok().entity(availability).build();
    }

    @Operation(
            summary = "Get status results for node services.",
            description = "Retrieve latest or historical status for a node’s services from Argo Web API.")
    @APIResponse(
            responseCode = "200",
            description = "Status retrieved successfully.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = WebApiNodeStatusResponse.class)))
    @APIResponse(
            responseCode = "401",
            description = "User has not been authenticated.",
            content = @Content(schema = @Schema(
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "403",
            description = "Not permitted.",
            content = @Content(schema = @Schema(
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "404",
            description = "Node not found.",
            content = @Content(schema = @Schema(
                    implementation = InformativeResponse.class)))
    @APIResponse(
            responseCode = "500",
            description = "Internal Server Error.",
            content = @Content(schema = @Schema(
                    implementation = InformativeResponse.class)))
    @SecurityRequirement(name = "Authentication")
    @GET
    @Path("/{name}/capabilities/status")
    @Produces(MediaType.APPLICATION_JSON)
    @SecuredEndpoint(bypass = true)
    public Response getStatus(
            @Parameter(description = "The name of the Node.",
                    required = true,
                    example = "GRNET",
                    schema = @Schema(type = SchemaType.STRING))
            @PathParam("name")
            String nodeName,
            @Parameter(name = "item", in = QUERY,
                    description = "Service name to target under the node.",
                    example = "WIKI")
            @QueryParam("item")
            String item,
            @Parameter(name = "start_time", in = QUERY,
                    description = "Start time in W3C format.")
            @QueryParam("start_time")
            String startTime,

            @Parameter(name = "end_time", in = QUERY,
                    description = "End time in W3C format.")
            @QueryParam("end_time")
            String endTime,

            @Parameter(name = "history", in = QUERY,
                    description = "Show full history of status timelines.",
                    example = "true")
            @QueryParam("history")
            Boolean history) {

        var status = nodeService.getStatusByNodeName(nodeName, item, startTime, endTime, history);

        return Response.ok().entity(status).build();
    }

    @Operation(
            summary = "Retrieve tenant node summary capability.",
            description = "Retrieves the daily availability and uptime summary for a specific service under the tenant node."
    )
    @APIResponse(
            responseCode = "200",
            description = "Tenant node summary details.",
            content = @Content(schema = @Schema(
                    type = SchemaType.OBJECT,
                    implementation = WebApiNodeSummaryResponse.class)))
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
    @SecurityRequirement(name = "Authentication")
    @GET
    @Path("/{name}/capabilities/summary/{item}")
    @Produces(MediaType.APPLICATION_JSON)
    @SecuredEndpoint(bypass = true)
    public Response getSummary(
            @Parameter(
                    description = "The name of the node.",
                    required = true,
                    example = "GRNET",
                    schema = @Schema(type = SchemaType.STRING))
            @PathParam("name")
            @Valid
            String nodeName,
            @Parameter(
                    description = "The service name to examine.",
                    required = true,
                    example = "WIKI",
                    schema = @Schema(type = SchemaType.STRING))
            @PathParam("item")
            String item,
            @Parameter(name = "start_date", in = QUERY,
                    description = "Start date (YYYY-MM-DD).")
            @QueryParam("start_date")
            @Valid
            @CheckDateFormat(pattern = "yyyy-MM-dd", message = "Valid date format is yyyy-MM-dd.")
            String startDate,

            @Parameter(name = "end_date", in = QUERY,
                    description = "End date (YYYY-MM-DD).")
            @QueryParam("end_date")
            @Valid
            @CheckDateFormat(pattern = "yyyy-MM-dd", message = "Valid date format is yyyy-MM-dd.")
            String endDate,

            @Parameter(name = "granularity", in = QUERY,
                    description = "Granularity of results (daily, monthly).",
                    example = "daily")
            @QueryParam("granularity")
            String granularity
    ) {

        var summary = nodeService.getSummaryByNodeName(nodeName, item, startDate, endDate, granularity);

        return Response.ok().entity(summary).build();
    }
}
