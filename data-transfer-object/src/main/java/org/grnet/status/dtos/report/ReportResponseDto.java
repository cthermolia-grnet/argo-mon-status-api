package org.grnet.status.dtos.report;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Response with name and description")
public class ReportResponseDto {

    @Schema(type = SchemaType.STRING,
            implementation = String.class,
            description = "ID of the report",
            example = "474f2d2b-c568-4ede-91a2-970d5be34d5f")
    public String id;

    @Schema(type = SchemaType.STRING,
            implementation = String.class,
            description = "Name of the report",
            example = "report1")
    public String name;

    @Schema(type = SchemaType.STRING,
            implementation = String.class,
            description = "Description of the report",
            example = "Report for usage")
    public String description;

    @Schema(type = SchemaType.STRING,
            implementation = String.class,
            description = "Name of the tenant",
            example = "tenant_name")
    @JsonProperty("tenant_name")
    public String tenantName;

    @Schema(type = SchemaType.STRING,
            implementation = String.class,
            description = "State of the report",
            example = "true")
    @JsonProperty("disabled")
    public String disabled;
//
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
