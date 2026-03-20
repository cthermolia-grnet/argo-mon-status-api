package org.grnet.status.dtos.argo;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name = "ArgoWebApiErrorResponse", description = "Represents an error response returned by Argo Web API.")
public class ArgoWebApiErrorResponse {

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Error message",
            example = "Node report not set")
    public String message;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Error code",
            example = "404")
    public int code;
}
