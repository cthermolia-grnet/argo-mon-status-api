package org.grnet.status.dtos.tenant.node;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name = "TenantNodeDto", description = "Represents tenant node information.")
public class TenantNodeDto {

    @Schema(
            type = SchemaType.STRING,
            description = "Node identifier",
            example = "9e2c9890-56c7-432a-bd6e-32e1da6eaa84-5"
    )
    @NotBlank(message = "Node id must not be empty")
    @JsonProperty("id")
    public String id;

    @Schema(
            type = SchemaType.STRING,
            description = "Node name",
            example = "GRNET"
    )
    @NotBlank(message = "Node name must not be empty")
    @JsonProperty("name")
    public String name;
}
