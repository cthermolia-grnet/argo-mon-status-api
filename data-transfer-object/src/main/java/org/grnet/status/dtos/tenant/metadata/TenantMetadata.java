package org.grnet.status.dtos.tenant.metadata;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;

@Schema(name = "TenantMetadata", description = "Represents the configuration of a tenant's metadata.")

public class TenantMetadata {

    @Schema(
            type = SchemaType.OBJECT,
            implementation = InstanceDto.class,
            description = "Instance"
    )
    @JsonProperty("instance")
    @Valid
   // @NotNull(message = "instance can not be null")
    public InstanceDto instance=new InstanceDto();

    @Schema(
            type = SchemaType.ARRAY,
            implementation = InternalListDto.class,
            description = "Internal list configuration"
    )
    @JsonProperty("internalLists")  // match the JSON field exactly
    @Valid      // <-- THIS IS REQUIRED for nested validation
    //@NotNull(message = "internal list can not be null")
    public List<InternalListDto> internalLists=new ArrayList<>();

    @Schema(
            type = SchemaType.OBJECT,
            implementation = AuthMetadataDto.class,
            description = "Auth Metadata"
    )
    @JsonProperty("auth_metadata")
    @Valid
    //@NotNull(message = "auth metadata can not be null")
    public AuthMetadataDto authMetadata=new AuthMetadataDto();

}
