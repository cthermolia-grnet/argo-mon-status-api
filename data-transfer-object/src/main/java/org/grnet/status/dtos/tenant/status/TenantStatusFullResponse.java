package org.grnet.status.dtos.tenant.status;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.grnet.status.dtos.readiness.TenantReadiness;

@Schema(name = "TenantStatusFullResponse", description = "Represents the response of a tenant's status with name info.")
public class TenantStatusFullResponse {
    @Schema(
            type = SchemaType.STRING,
            description = "Tenant's name",
            example = "DEMO"
    )
    @JsonProperty("name")
    public String name;


    @Schema(
            type = SchemaType.OBJECT,
            implementation = TenantStatusDto.class,
            description = "Tenant Status "
    )
    @JsonProperty("status")
    @Valid
    public TenantStatusDto status;


    @Schema(
            type = SchemaType.OBJECT,
            implementation = TenantReadiness.class,
            description = "Tenant Readiness "
    )
    @JsonProperty("readiness")
    @Valid
    public TenantReadiness readiness;

}