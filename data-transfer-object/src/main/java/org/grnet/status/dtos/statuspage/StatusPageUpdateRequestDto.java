package org.grnet.status.dtos.statuspage;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;


@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Request to update an existing status statuspage")
public class StatusPageUpdateRequestDto {

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Page name",
            example = "Test Page"
    )
    @JsonProperty("name")
    @NotBlank(message = "name cannot be blank")
    public String name;
    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Slug of the statuspage (used in URL)",
            example = "argo-status"
    )
    @JsonProperty("slug")
    @NotBlank(message = "slug cannot be blank")
    public String slug;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Report Identifier to retrieve groups from",
            example = "Critical"
    )
    @JsonProperty("report-id")
    @NotBlank(message = "report cannot be blank")
    public String reportId;

    @Schema(
            type = SchemaType.OBJECT,
            description = "JSON configuration object (groups, theming, description, etc.)"
    )
    @JsonProperty("config")
    @NotNull(message = "config must not be null")
    @Valid
    public StatusPageConfigDto config;
}