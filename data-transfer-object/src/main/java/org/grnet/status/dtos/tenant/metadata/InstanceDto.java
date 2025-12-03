package org.grnet.status.dtos.tenant.metadata;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.grnet.status.constraints.ValidContactType;
import org.grnet.status.constraints.ValidTopologyType;
import org.hibernate.validator.constraints.URL;

@Schema(name = "InstanceDto", description = "Represents the configuration of a tenant's metadata instance info.")

public class InstanceDto {

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "UI url",
            example="https://example.ui.com"
    )
    @JsonProperty("ui_url")
    @Valid
    @URL(message = "Invalid URL format")
    @NotNull(message = "instance ui url can not be null")

    public String uiUrl;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "poem url",
            examples = "https://poem.example.gr"
    )
    @JsonProperty("poem_url")
    @Valid
    @URL(message = "Invalid URL format")
    @NotNull(message = "poem can not be null")
    public String poemUrl;


    @Schema(
            type = SchemaType.OBJECT,
            implementation = TenantTopologyDto.class,
            description = "Topology"
    )
    @JsonProperty("topology")
    @Valid
    @NotNull(message = "instance topology can not be null")
    public TenantTopologyDto topology;


}
