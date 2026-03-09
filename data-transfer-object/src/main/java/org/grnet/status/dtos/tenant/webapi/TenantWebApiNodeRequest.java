package org.grnet.status.dtos.tenant.webapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.grnet.status.dtos.tenant.node.TenantNodeDto;

@Schema(name = "TenantWebApiNodeRequest", description = "Represents a tenant node update request.")
public class TenantWebApiNodeRequest {

    @Schema(
            type = SchemaType.BOOLEAN,
            implementation = Boolean.class,
            description = "Indicates whether the tenant is configured as a node",
            example = "true"
    )
    @JsonProperty("node")
    //@Valid
    public Boolean node;
}
