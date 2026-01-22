package org.grnet.status.dtos.tenant;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.Instant;


@Schema(name = "TenantInfoRequestDto", description = "Represents the configuration of a tenant info.")
public class TenantInfoDto {


    @Schema(
            type = SchemaType.STRING,
            description = "Tenant's name",
            example = "GRNET"
    )
    @NotBlank(message = "Name must not be empty")
    @Pattern(
            regexp = "^(?=.*[A-Z])[A-Z0-9\\-]+$",
            message = "Name must contain only uppercase Latin letters, numbers, or hyphens"
    )
    @JsonProperty("name")

    public String name;

    @Schema(
            type = SchemaType.STRING,
            description = "Tenant's email",
            example = "grnet@gmail.com"
    )
    @Email(message = "Email must be a valid email address")
    @NotBlank(message = "Email must not be empty")
    @JsonProperty("email")
    public String email;

    @Schema(
            type = SchemaType.STRING,
            description = "Tenant's description",
            example = "This is the GRNET tenant"
    )
    @NotBlank(message = "Description must not be empty")
    @JsonProperty("description")
    public String description;

    @Schema(
            type = SchemaType.STRING,
            description = "Tenant's website",
            example = "https://www.grnet.gr"
    )
    @Pattern(
            regexp = "^$|^(https?://).+",
            message = "Website must be empty or start with http:// or https://"
    )
    @JsonProperty("website")
    public String website;

    @Schema(
            type = SchemaType.STRING,
            description = "Tenant's image URL or base64 encoded image data URI. " +
                    "Example URL: /images/profile.jpg, or data URI: data:image/png;base64,iVBORw0KGgo...",
            example = "https://images/profile.jpg"
    )
    @Pattern(
            regexp = "(^https?://.*\\.(jpg|jpeg|png|gif)(\\?.*)?$)|(^data:image\\/([a-zA-Z]+);base64,[A-Za-z0-9+/=]+$)",
            message = "Must be a valid image URL (jpg, jpeg, png, gif) or base64 encoded data URI",
            flags = Pattern.Flag.CASE_INSENSITIVE
    )
    @JsonProperty("image")
    public String image;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Timestamp of creation",
            example = "2025-10-22T12:44:48.107Z"
    )
    @JsonProperty("created_at")
    public Instant createdAt;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Timestamp of last update",
            example = "2025-10-22T12:44:48.107Z"
    )
    @JsonProperty("updated_at")
    public Instant updatedAt;
}
