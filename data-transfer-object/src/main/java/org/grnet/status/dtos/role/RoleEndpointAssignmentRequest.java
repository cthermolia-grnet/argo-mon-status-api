package org.grnet.status.dtos.role;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;
@Schema(name = "RoleEndpointAssignementRequest", description = "Represents the request to assign secured endpoints on roles")
@Getter
@Setter
public class RoleEndpointAssignmentRequest {


    private List<RoleAssignment> assignments;

    @Getter
    @Setter
    public static class RoleAssignment {

        @Schema(
                type = SchemaType.STRING,
                implementation = String.class,
                description = "Role id",
                example = "123445-129393-13948"
        )
        @JsonProperty("role_id")
        @NotBlank(message = "Role id cannot be blank")

        private String roleId;
        @Schema(
                type = SchemaType.STRING,
                implementation = String.class,
                description = "Role name",
                example = "TENANT-ADMIN"
        )
        @JsonProperty("role_name")
        @NotBlank(message = "Role name cannot be blank")

        private String roleName;
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


}