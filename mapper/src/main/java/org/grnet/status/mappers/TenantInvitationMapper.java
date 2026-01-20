package org.grnet.status.mappers;

import org.grnet.status.dtos.tenant.invitations.TenantInvitationRequest;
import org.grnet.status.dtos.tenant.invitations.TenantInvitationResponse;
import org.grnet.status.entities.TenantInvitation;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Mapper(imports = {Timestamp.class, Instant.class})
public interface TenantInvitationMapper {

    TenantInvitationMapper INSTANCE = Mappers.getMapper(TenantInvitationMapper.class);

    @IterableMapping(qualifiedByName = "mapToResponse")
    List<TenantInvitationResponse> listToDtos(List<TenantInvitation> tenantInvitations);

    @Named("mapToResponse")
    @Mapping(target = "id", source = "id")
    @Mapping(target = "tenantId", source = "tenant.id")
    @Mapping(target = "tenantName", source = "tenant.name")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "role", source = "role")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "createdAt", source = "createdAt")
    TenantInvitationResponse tenantInvitationToDto(TenantInvitation tenantInvitation);

    @Named("mapToEntity")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", source = "email")
    @Mapping(target = "role", source = "role")
    TenantInvitation tenantInvitationToEntity(TenantInvitationRequest request);
}

