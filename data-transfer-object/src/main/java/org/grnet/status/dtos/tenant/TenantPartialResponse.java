package org.grnet.status.dtos.tenant;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name = "TenantPartialResponse", description = "Represents the partial response of a tenant.")
public class TenantPartialResponse {
    @Schema(
            type = SchemaType.STRING,
            description = "Tenant's id",
            example = "9e2c9890-56c7-432a-bd6e-32e1da6eaa84-5"
    )
    @JsonProperty("id")
    public String id;

    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "Tenant Web ApiInfo "
    )
    @JsonProperty("name")
    @Valid
    public String name;

    public TenantPartialResponse(String tenantId, String tenantName) {
    }
}