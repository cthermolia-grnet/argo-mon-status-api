package org.grnet.status.dtos.statuspage;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name = "StatusPageThemingDto", description = "UI configuration and theming options.")
public class StatusPageThemingDto {

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Theme option",
            example = "theme_1"
    )
    @JsonProperty("option")
    @NotNull(message = "option must not be null")
    @Valid
    public String option;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Logo for the status page — can be a Base64 data URI or HTTPS URL",
            example = "data:image/png;base64,iVBORw0KGgoAAA... or https://example.com/logo.png"
    )
    @JsonProperty("logo")
    public String logo;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Page background or main color",
            example = "#ffffff"
    )
    @JsonProperty("color")
    @NotBlank(message = "theming color cannot be blank")
    public String color;

    @Schema(
            type = SchemaType.OBJECT,
            implementation = StatusPageThemingStatusDto.class,
            description = "Status display options (icon, text, etc.)"
    )
    @JsonProperty("status")
    @NotNull(message = "theming status cannot be blank")
    @Valid
    public StatusPageThemingStatusDto status;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Number of columns for layout",
            example = "one"
    )
    @JsonProperty("columns")
    @NotBlank(message = "theming columns cannot be blank")
    public String columns;
}
