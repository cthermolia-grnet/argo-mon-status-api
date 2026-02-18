package org.grnet.status.dtos.report;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Response with name and description")
public class PartialReportResponseDto {

    @Schema(type = SchemaType.STRING,
            implementation = String.class,
            description = "Identifier of the report",
            example = "Report Id")
    @JsonProperty("id")
    public String id;

    @Schema(type = SchemaType.STRING,
            implementation = String.class,
            description = "Name of the report",
            example = "report1")
    @JsonProperty("name")
    public String name;

    @Schema(type = SchemaType.STRING,
            implementation = String.class,
            description = "Description of the report",
            example = "Report for usage")
    @JsonProperty("description")
    public String description;

    @Schema(type = SchemaType.STRING,
            implementation = String.class,
            description = "Name of the tenant",
            example = "tenant_name")
    @JsonProperty("tenant_name")
    public String tenantName;

    @Schema(type = SchemaType.BOOLEAN,
            implementation = Boolean.class,
            description = "State of the report",
            example = "true")
    @JsonProperty("disabled")
    public Boolean disabled;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Timestamp of creation",
            example = "2025-10-22T12:44:48.107Z"
    )
    @JsonProperty("created_at")
    public String createdAt;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Timestamp of last update",
            example = "2025-10-22T12:44:48.107Z"
    )
    @JsonProperty("updated_at")
    public String updatedAt;
}
