package org.grnet.status.dtos.tenant.invitations;

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
}
