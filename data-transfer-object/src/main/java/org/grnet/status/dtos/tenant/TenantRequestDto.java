package org.grnet.status.dtos.tenant;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.grnet.status.dtos.tenant.metadata.TenantMetadata;

import java.util.List;

@Schema(name = "TenantRequestDto", description = "Represents the configuration of a tenant.")
@Getter
@Setter
public class TenantRequestDto {

    @Schema(
            type = SchemaType.OBJECT,
            implementation = TenantInfoDto.class,
            description = "Tenant Info "
    )
    @JsonProperty("info")
    @Valid
    public TenantInfoDto info;

    @JsonProperty("contacts")
    @Valid
    public List<ContactDto> contacts;


    @Schema(
            type = SchemaType.OBJECT,
            implementation = TenantMetadata.class,
            description = "Metadata Info "
    )
    @JsonProperty("metadata")
    @Valid
    public TenantMetadata metadata;
}