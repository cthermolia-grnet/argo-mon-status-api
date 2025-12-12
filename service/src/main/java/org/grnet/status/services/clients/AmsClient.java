package org.grnet.status.services.clients;


import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.grnet.status.dtos.ams.PublishRequest;
import org.grnet.status.dtos.ams.PublishResponse;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface AmsClient {

    @POST
    @Path("/v1/projects/{project}/topics/{topic}:publish")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    PublishResponse publish(
            @HeaderParam("x-api-key") String apiKey,
            @PathParam("project") String project,
            @PathParam("topic") String topic,
            PublishRequest request
    );
}
