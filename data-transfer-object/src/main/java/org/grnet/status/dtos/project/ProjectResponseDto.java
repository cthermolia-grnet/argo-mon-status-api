package org.grnet.status.dtos.project;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.time.Instant;

public class ProjectResponseDto {

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Project identifier",
            example = "proj-32262f66f6e1"
    )
    @JsonProperty("id")
    public String id;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Name of the project",
            example = "EOSC-Future"
    )
    @JsonProperty("name")
    public String name;

    @Schema(
            type = SchemaType.STRING,
            implementation = Instant.class,
            description = "Project start date"
    )
    @JsonProperty("start_date")
    public Instant startDate;

    @Schema(
            type = SchemaType.STRING,
            implementation = Instant.class,
            description = "Project end date"
    )
    @JsonProperty("end_date")
    public Instant endDate;

    @Schema(
            type = SchemaType.STRING,
            implementation = Instant.class,
            description = "Sustainability end date"
    )
    @JsonProperty("sustainability_end_date")
    public Instant sustainabilityEndDate;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Data retention policy"
    )
    @JsonProperty("data_retention_policy")
    public String dataRetentionPolicy;

    @Schema(
            type = SchemaType.STRING,
            implementation = Instant.class,
            description = "Timestamp when the project was created"
    )
    @JsonProperty("created_at")
    public Instant createdAt;

    @Schema(
            type = SchemaType.STRING,
            implementation = Instant.class,
            description = "Timestamp when the project was last updated"
    )
    @JsonProperty("updated_at")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Instant updatedAt;
}
