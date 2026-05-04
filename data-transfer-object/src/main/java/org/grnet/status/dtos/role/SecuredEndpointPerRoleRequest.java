package org.grnet.status.dtos.role;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

@Getter
@Setter
@Schema(name = "SecuredEndpointPerRoleRequest", description = "Represents the request to assign secured endpoints on role")

public class SecuredEndpointPerRoleRequest {
@Schema(
                type = SchemaType.ARRAY,
                implementation = String.class,
                description = "List of secured endpoint ids",
                example = "[\"123445-129393-13948\", \"223445-129393-13999\"]"
        )
        @JsonProperty("secured_endpoint_ids")
        @NotEmpty
        private List<String> securedEndpointIds;
}
