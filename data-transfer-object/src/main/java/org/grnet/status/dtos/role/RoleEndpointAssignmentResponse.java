package org.grnet.status.dtos.role;



import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

@Getter
@Setter
@Schema(name = "RoleEndpointAssignmentResponse", description = "Represents the response of assigned secured endpoints to role")

public class RoleEndpointAssignmentResponse {

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<RoleAssignment> assignments;

    @Getter
    @Setter
    public static class RoleAssignment {

        @JsonProperty("role_id")
        private String roleId;

        @JsonProperty("role_name")
        private String roleName;

        @JsonProperty("secured_endpoint_ids")
        private List<String> securedEndpointIds;
    }
}
