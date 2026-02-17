package org.grnet.status.dtos.statuspage;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Response DTO representing a status statuspage")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StatusPageResponseDto {

    @Schema(
            type = SchemaType.NUMBER,
            implementation = String.class,
            description = "Unique ID of the Status Page",
            example = "9cb6e2a4-233b-4613-8116-aa3d9a900494"
    )
    @JsonProperty("id")
    public String id;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Page name",
            example = "ARGO Status Page"
    )
    @JsonProperty("name")
    public String name;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Slug of the statuspage (used in URL)",
            example = "argo-status"
    )
    @JsonProperty("slug")
    public String slug;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "User ID of the creator",
            example = "alice_voperson_id"
    )
    @JsonProperty("user_id")
    public String userId;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Report name to display status groups for",
            example = "Critical"
    )
    @JsonProperty("report")
    public String report;

    @Schema(
            type = SchemaType.OBJECT,
            description = "JSON configuration object (groups, theming, description, etc.)"
    )
    @JsonProperty("config")
    public StatusPageConfigDto config;

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

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Tenant's Identifier",
            example = "9e2c9890-56c7-432a-bd6e-32e1da6eaa84-5"
    )
    @JsonProperty("tenant_id")
    public String tenantId;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Tenant's Name",
            example = "GRNET"
    )
    @JsonProperty("tenant_name")
    public String tenantName;


}
