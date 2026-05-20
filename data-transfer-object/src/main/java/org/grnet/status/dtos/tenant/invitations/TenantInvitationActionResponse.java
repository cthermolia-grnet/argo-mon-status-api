package org.grnet.status.dtos.tenant.invitations;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.grnet.status.enums.InvitationAction;
import org.grnet.status.enums.InvitationStatus;

public class TenantInvitationActionResponse {

    @Schema(
            type = SchemaType.OBJECT,
            implementation = InvitationStatus.class,
            description = "Invitation action",
            enumeration = {"ACCEPT", "REJECT"},
            example = "ACCEPT"
    )
    @NotNull
    public InvitationAction action;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "The resource name",
            example = "Tenant"
    )
    @JsonProperty("api_resource")
    public String apiResource;

    @Schema(
            type = SchemaType.OBJECT,
            implementation = Object.class,
            description = "The resource id",
            example = "5"
    )
    @JsonProperty("resource_id")
    public String resourceId;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "The role name",
            example = "admin"
    )
    @NotEmpty(message = "role may not be empty.")
    public String role;
}
