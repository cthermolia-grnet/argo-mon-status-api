package org.grnet.status.dtos.argo.web.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Request to connect to Argo Web Api")
public class ArgoWebApiRequest {

    @Schema(type = SchemaType.STRING,
            implementation = String.class,
            description = "API identifier or URL",
            example = "api")

    @Pattern(
            regexp = "^(https?://)?([\\w\\-]+\\.)+[\\w\\-]{2,}(/\\S*)?$",
            message = "API identifier must be a valid URL")
    @NotNull(message = "API identifier must not be null")
    @NotBlank(message = "API identifier must not be empty ")
    public String api;

    @Schema(type = SchemaType.STRING,
            implementation = String.class,
            description = "Secret used to authenticate",
            example = "U2s3cr3tKeY")
    @NotNull(message = "Secret must not be null")
    @NotBlank(message = "Secret must not be empty")
    public String secret;
}
