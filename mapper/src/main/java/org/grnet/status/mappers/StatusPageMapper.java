package org.grnet.status.mappers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.grnet.status.dtos.statuspage.StatusPageConfigDto;
import org.grnet.status.dtos.statuspage.StatusPageRequestDto;
import org.grnet.status.dtos.statuspage.StatusPageUpdateRequestDto;
import org.grnet.status.dtos.statuspage.StatusPageResponseDto;
import org.grnet.status.entities.StatusPage;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Mapper(imports = {StringUtils.class, Timestamp.class, Instant.class})
public interface StatusPageMapper {

    StatusPageMapper INSTANCE = Mappers.getMapper(StatusPageMapper.class);

    ObjectMapper MAPPER = new ObjectMapper();

    /* ─────────────── ENTITY → DTO ─────────────── */

    @IterableMapping(qualifiedByName = "mapEntity")
    List<StatusPageResponseDto> entitiesToDtos(List<StatusPage> entities);

    @Named("mapEntity")
    @Mapping(target = "config", expression = "java(jsonToConfig(entity.getConfig()))")
    @Mapping(target = "tenantId", source = "tenant.id")
    @Mapping(target = "tenantName", source = "tenant.name")
    StatusPageResponseDto entityToDto(StatusPage entity);


    /* ─────────────── CREATE ─────────────── */

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "report", ignore = true)
    @Mapping(target = "createdAt", expression = "java(Timestamp.from(Instant.now()))")
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "slug", source = "slug")
    @Mapping(target = "config", expression = "java(configToJson(dto.config))")
    StatusPage dtoToEntity(StatusPageRequestDto dto);


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "report", ignore = true)
    @Mapping(target = "name", expression = "java(StringUtils.isNotBlank(dto.name) ? dto.name : entity.getName())")
    @Mapping(target = "slug", expression = "java(StringUtils.isNotBlank(dto.slug) ? dto.slug : entity.getSlug())")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", expression = "java(Timestamp.from(Instant.now()))")
    @Mapping(target = "config", expression = "java(configToJson(dto.config))")
    void updateToEntity(StatusPageUpdateRequestDto dto, @MappingTarget StatusPage entity);


    @SneakyThrows
    default String configToJson(StatusPageConfigDto dto) {
        if (dto == null) return null;
        return MAPPER.writeValueAsString(dto);
    }

    @SneakyThrows
    default StatusPageConfigDto jsonToConfig(String json) {
        if (json == null || json.isBlank()) return null;
        return MAPPER.readValue(json, StatusPageConfigDto.class);
    }


    default Instant map(Timestamp timestamp) {
        return timestamp != null ? timestamp.toInstant() : null;
    }

    default Timestamp map(Instant instant) {
        return instant != null ? Timestamp.from(instant) : null;
    }
}
