package org.grnet.status.mappers;

import org.grnet.status.dtos.tenantproject.TenantProjectDto;
import org.grnet.status.entities.TenantProjectJunction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface TenantProjectMapper {

    TenantProjectMapper INSTANCE = Mappers.getMapper(TenantProjectMapper.class);

    List<TenantProjectDto> listToDtos(List<TenantProjectJunction> projects);

    @Mapping(source = "tenant.id", target = "tenantId")
    @Mapping(source = "tenant.name", target = "tenantName")
    @Mapping(source = "project.id", target = "projectId")
    @Mapping(source = "project.name", target = "projectName")
    TenantProjectDto assignToDto(TenantProjectJunction entity);
}

