package org.grnet.status.dtos.tenant;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.grnet.status.dtos.tenant.metadata.TenantMetadata;
import java.util.List;

@Schema(name = "TenantResponseDto", description = "Represents the response of a tenant.")
public class TenantResponseDto {
    @Schema(
            type = SchemaType.STRING,
            description = "Tenant's id",
            example = "9e2c9890-56c7-432a-bd6e-32e1da6eaa84-5"
    )
    @JsonProperty("id")
    public String id;

    @Schema(
            type = SchemaType.OBJECT,
            implementation = TenantInfoDto.class,
            description = "Tenant Web ApiInfo "
    )
    @JsonProperty("info")
    @Valid
    public TenantInfoDto info;


    @Schema(
            type = SchemaType.STRING,
            implementation = String.class,
            description = "User ID of the creator",
            example = "alice_voperson_id"
    )
    @JsonProperty("updated_by")
    public String updatedBy;

    @Schema(type = SchemaType.ARRAY, implementation = ContactDto.class, description = "List of contacts")
    @JsonProperty("contacts")
    public List<ContactDto> contacts;

    @Schema(
            type = SchemaType.OBJECT,
            implementation = TenantMetadata.class,
            description = "Tenant Metadata "
    )
    @JsonProperty("metadata")
    @Valid
    public TenantMetadata metadata;
}