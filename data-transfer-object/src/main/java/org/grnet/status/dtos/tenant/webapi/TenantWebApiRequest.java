package org.grnet.status.dtos.tenant.webapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.grnet.status.dtos.tenant.DBConfDto;
import org.grnet.status.dtos.tenant.TenantInfoDto;
import org.grnet.status.dtos.tenant.node.TenantNodeDto;
import org.grnet.status.dtos.tenant.UserDto;
import org.grnet.status.dtos.tenant.metadata.TenantTopologyDto;

import java.util.Arrays;
import java.util.List;

@Schema(name = "TenantWebApiRequest", description = "Represents the configuration of a tenant.")
public class TenantWebApiRequest {

    @Schema(
            type = SchemaType.OBJECT,
            implementation = TenantInfoDto.class,
            description = "Tenant Info "
    )
    @JsonProperty("info")
    @Valid
    public TenantInfoDto info;

    @Schema(
            type = SchemaType.OBJECT,
            implementation = TenantTopologyDto.class,
            description = "Tenant topology "
    )
    @JsonProperty("topology")
    @Valid
    public TenantTopologyDto topology;



    @Schema(
            type = SchemaType.ARRAY,
            implementation = Arrays.class,
            description = "Tenant users "
    )
    @JsonProperty("users")
    @Valid
    public List<UserDto> users;


    @Schema(
            type = SchemaType.ARRAY,
            implementation = Arrays.class,
            description = "Tenant db conf "
    )
    @JsonProperty("db_conf")
    @Valid
    public List<DBConfDto> db_conf;

    @Schema(
            type = SchemaType.BOOLEAN,
            implementation = Boolean.class,
            description = "Indicates whether the tenant is configured as a node",
            example = "true"    )
    @JsonProperty("node")
    @Valid
    public Boolean node;
}