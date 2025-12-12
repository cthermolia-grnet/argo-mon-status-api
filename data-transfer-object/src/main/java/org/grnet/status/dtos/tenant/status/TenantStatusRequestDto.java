package org.grnet.status.dtos.tenant.status;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.List;

public class TenantStatusRequestDto {
    @Schema(
            type = SchemaType.OBJECT,
            implementation = TenantStatusDto.class,
            description = ""
    )
    @JsonProperty("status")
    public TenantStatusDto status;

}
