package org.grnet.status.dtos.report;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;


@Schema(description = "Request to fetch reports from a target API")
public class ReportRequestDto {

    @Schema(type = SchemaType.STRING,
            implementation = String.class,
            description = "API identifier or URL",
            example = "api")
    @Pattern(
            regexp = "^(https?://)?([\\w\\-]+\\.)+[\\w\\-]{2,}(/\\S*)?$",
            message = "API identifier must be a valid URL")
    @NotBlank(message = "API identifier must not be empty ")
    public String api;

    @Schema(type = SchemaType.STRING,
            implementation = String.class,
            description = "Secret used to authenticate",
            example = "U2s3cr3tKeY")
    @NotBlank(message = "secret cannot be blank")
    public String secret;
}
