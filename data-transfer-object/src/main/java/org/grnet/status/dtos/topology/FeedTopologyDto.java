package org.grnet.status.dtos.topology;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(name = "FeedTopologyDto", description = "Represents the topology feed configuration.")
public class FeedTopologyDto {

    @Schema(
            type = SchemaType.STRING,
            description = "Connector type used for topology ingestion.",
            example = "CSV"
    )
    @JsonProperty("type")
    @NotBlank(message = "topology feed type can not be blank")
    public String type;

    @Schema(
            type = SchemaType.STRING,
            description = "EOSC service catalog feed URL for service groups.",
            example = "https://somewhere2.foo.bar/service_groups"
    )
    @JsonProperty("feed_service_groups")
    public String feedServiceGroups;

    @Schema(
            type = SchemaType.STRING,
            description = "EOSC service catalog feed URL for service endpoints.",
            example = "https://somewhere2.foo.bar/service_endpoints"
    )
    @JsonProperty("feed_service_endpoints")
    public String feedServiceEndpoints;

    @Schema(
            type = SchemaType.STRING,
            description = "EOSC service catalog feed URL for service endpoint extensions.",
            example = "https://somewhere2.foo.bar/service_endpoints_extensions"
    )
    @JsonProperty("feed_service_endpoints_extensions")
    public String feedServiceEndpointsExtensions;

    @Schema(
            type = SchemaType.STRING,
            description = "CSV topology feed URL.",
            example = "https://docs.google.com/spreadsheets/d/example/export?gid=0&format=csv"
    )
    @JsonProperty("feed_url")
    public String feedUrl;

    @Schema(
            type = SchemaType.STRING,
            description = "Whether the CSV feed is paginated.",
            example = "false"
    )
    @JsonProperty("paginated")
    public String paginated;

    @Schema(
            type = SchemaType.ARRAY,
            description = "Topology object types to fetch from the CSV feed.",
            example = "[\"ServiceGroups\"]"
    )
    @JsonProperty("fetch_type")
    public List<String> fetchType;

    @Schema(
            type = SchemaType.STRING,
            description = "Endpoint UID field used by the CSV connector.",
            example = ""
    )
    @JsonProperty("uid_endpoints")
    public String uidEndpoints;
}