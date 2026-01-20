package org.grnet.status.dtos.tenant.invitations;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.grnet.status.enums.InvitationStatus;

import java.time.Instant;

public class TenantInvitationResponse {
    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Invitation Id"
    )
    @JsonProperty("id")
    public String id;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Tenant Id",
            example = "9e2c9890-56c7-432a-bd6e-32e1da6eaa84-5"
    )
    @JsonProperty("tenant_id")
    public String tenantId;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Tenant Name",
            example = "9e2c9890-56c7-432a-bd6e-32e1da6eaa84-5"
    )
    @JsonProperty("tenant_name")
    public String tenantName;

    @Schema(
            type = SchemaType.STRING,
            description = "Recipient's email",
            example = "grnet@gmail.com"
    )
    @Email(message = "Email must be a valid email address")
    @NotBlank(message = "Email must not be empty")
    @JsonProperty("email")
    public String email;
    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Tenant invitation role",
            example = "viewer",
            enumeration = {"admin", "viewer"}
    )
    @JsonProperty("role")
    public String role;

    @Schema(
            type = SchemaType.OBJECT,
            implementation = InvitationStatus.class,
            description = "Invitation status"
    )
    @JsonProperty("status")
    public InvitationStatus status;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Timestamp of creation",
            example = "2025-10-22T12:44:48.107Z"
    )
    @JsonProperty("created_at")
    public Instant createdAt;

}
