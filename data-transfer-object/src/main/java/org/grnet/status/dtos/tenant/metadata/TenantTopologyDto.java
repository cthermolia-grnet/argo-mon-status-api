package org.grnet.status.dtos.tenant.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.grnet.status.constraints.ValidTopologyType;
import org.hibernate.validator.constraints.URL;

import java.util.Locale;

@Schema(name = "TopologyDto", description = "Represents the configuration of a tenant's metadata topology info.")

public class TenantTopologyDto {


    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "type",
            example="CSV"
    )
    @JsonProperty("type")
    @Valid
    @NotNull(message = "topology type can not be null")
    @ValidTopologyType
    public String type;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "url",
            examples ="https://example.com"
    )
    @JsonProperty("url")
    @Valid
    @NotNull(message = "topology url can not be null")
    @URL(message = "Invalid URL format")
    public String url;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "feed",
            example="gocdb1.example.foo"
    )
    @JsonProperty("feed")
    @Valid
    @NotNull(message = "topology feed can not be null")
    public String feed;
}
