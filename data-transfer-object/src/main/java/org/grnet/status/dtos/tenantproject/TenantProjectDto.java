package org.grnet.status.dtos.tenantproject;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.sql.Timestamp;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TenantProjectDto {

    @Schema(description = "ID of the assignment",
            type = SchemaType.STRING,
            example = "0b5bdbda-2de8-454f-b4d3-f2b47dd5872f")
    public String id;

    @Schema(
            type = SchemaType.STRING,
            description = "Tenant's id",
            example = "9e2c9890-56c7-432a-bd6e-32e1da6eaa84-5"
    )
    @JsonProperty("tenant_id")
    public String tenantId;


    @Schema(
            type = SchemaType.STRING,
            description = "Tenant's name",
            example = "GRNET"
    )
    @JsonProperty("tenant_name")
    public String tenantName;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Project identifier",
            example = "proj-32262f66f6e1"
    )
    @JsonProperty("project_id")
    public String projectId;

    @Schema(description = "Timestamp of assignment")
    @JsonProperty("assigned_at")
    public Timestamp assignedAt;

    @Schema(description = "User who assigned the project to the tenant")
    @JsonProperty("created_by")
    public String createdBy;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Name of the project",
            example = "EOSC-Future"
    )
    @JsonProperty("project_name")
    public String projectName;
}
