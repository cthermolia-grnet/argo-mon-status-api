package org.grnet.status.mappers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Inject;
import org.grnet.status.dtos.tenant.*;
import org.grnet.status.dtos.tenant.metadata.TenantMetadata;
import org.grnet.status.dtos.tenant.webapi.TenantWebApiGetResponse;
import org.grnet.status.dtos.tenant.webapi.TenantWebApiRequest;
import org.grnet.status.entities.Contact;
import org.grnet.status.entities.ContactTenantJunction;
import org.grnet.status.entities.Tenant;
import org.grnet.status.entities.TenantPartial;
import org.grnet.status.enums.ContactType;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Mapper(imports = {Timestamp.class, Instant.class})
public interface TenantMapper {
    @Inject
    ObjectMapper objectMapper = new ObjectMapper();

    TenantMapper INSTANCE = Mappers.getMapper(TenantMapper.class);
    static final DateTimeFormatter DATE_TIME_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("UTC"));


    @Mapping(target = "info", source = "info")
    @Mapping(target = "topology", source = "metadata.instance.topology")
    TenantWebApiRequest toWebApiRequest(TenantRequestDto dto);


    default List<TenantResponseDto> webApiTenantsToDtos(
            List<Tenant> tenants,
            List<TenantWebApiGetResponse> webApiGetResponses
    ) throws JsonProcessingException {
        List<TenantResponseDto> dtos = new ArrayList<>();

        // Build map: tenantId -> Tenant
        Map<String, Tenant> tenantMap = tenants.stream()
                .collect(Collectors.toMap(t -> t.id, t -> t));

        for (TenantWebApiGetResponse response : webApiGetResponses) {

            // Ensure response contains data
            if (response.getData() == null || response.getData().isEmpty()) {
                continue;
            }

            var tenant = tenantMap.get(response.getData().get(0).getId());

            if (tenant != null) {
                dtos.add(webApiTenantToDto(tenant, response));
            }
        }

        return dtos;
    }

    @Named("map")
    default TenantResponseDto webApiTenantToDto(Tenant tenant, TenantWebApiGetResponse webApiGetResponse) throws JsonProcessingException {
        TenantResponseDto dto = new TenantResponseDto();
        dto.id = tenant.id;
        TenantInfoDto dtoInfo = new TenantInfoDto();
        dtoInfo.name = webApiGetResponse.getData().get(0).getInfo().getName();
        dtoInfo.email = webApiGetResponse.getData().get(0).getInfo().getEmail();
        dtoInfo.website = webApiGetResponse.getData().get(0).getInfo().getWebsite();
        dtoInfo.description = webApiGetResponse.getData().get(0).getInfo().getDescription();
        dtoInfo.image = webApiGetResponse.getData().get(0).getInfo().getImage();
        dtoInfo.createdAt = Instant.from(DATE_TIME_FMT.parse(webApiGetResponse.getData().get(0).getInfo().getCreated()));
        dtoInfo.updatedAt = Instant.from(DATE_TIME_FMT.parse(webApiGetResponse.getData().get(0).getInfo().getUpdated()));
        dto.info = dtoInfo;
        dto.updatedBy = tenant.updatedBy;
        dto.metadata = mapMetadataObject(tenant.getMetadata());
        if (dto.metadata != null && dto.metadata.instance!=null && dto.metadata.instance.topology!=null) {
            dto.metadata.instance.topology.feed = webApiGetResponse.getData().get(0).getTopology().getFeed();
        }
        return dto;
    }

    @Named("contactToDto")
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "contactName")
    @Mapping(target = "email", source = "contactEmail")
    @Mapping(target = "type", source = "contactType", qualifiedByName = "mapTypeToString")
    ContactDto contactToDto(Contact contact);

    @Named("contactsToDtos")
    default List<ContactDto> contactsToDtos(Set<Contact> contacts) {
        if (contacts == null) {
            return List.of();
        }
        return contacts.stream()
                .map(this::contactToDto)
                .collect(Collectors.toList());
    }

    @Named("contactsFullToDtos")
    default List<ContactFullDto> contactsFullToDtos(Set<ContactTenantJunction> contactTenantJunctions) {
        if (contactTenantJunctions == null) {
            return List.of();
        }
        return contactTenantJunctions.stream()
                .map(this::contactFullToDto)
                .collect(Collectors.toList());
    }
    @Named("contactFullToDto")
    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "type", source = "type", qualifiedByName = "mapTypeToString")
    @Mapping(target = "tenants", source = "tenants", qualifiedByName = "tenantPartialsToResponses")
    ContactFullDto contactFullToDto(ContactTenantJunction contactTenantJunction);




    @Named("tenantPartialToResponse")
    TenantPartialResponse tenantPartialToResponse(TenantPartial tenantPartial);

    @Named("tenantPartialsToResponses")
    default List<TenantPartialResponse> tenantPartialsToResponses(List<TenantPartial> tenants) {
        if (tenants == null) {
            return Collections.emptyList();
        }
        return tenants.stream()
                .map(this::tenantPartialToResponse)
                .collect(Collectors.toList());
    }
    @Named("mapTypeToString")
    default String mapTypeToString(ContactType contactType) {
        return contactType != null ? contactType.name() : null;
    }

    @IterableMapping(qualifiedByName = "map1")
    List<TenantResponseDto> tenantsToDtos(List<Tenant> tenants);

    @Named("map1")
    @Mapping(target = "id", source = "id")
    @Mapping(target = "info.name", source = "name")
    @Mapping(target = "info.email", source = "email")
    @Mapping(target = "info.website", source = "website")
    @Mapping(target = "info.description", source = "description")
    @Mapping(target = "info.image", source = "image")
    @Mapping(target = "info.createdAt", source = "createdAt")
    @Mapping(target = "info.updatedAt", source = "updatedAt")
    @Mapping(target = "updatedBy", source = "updatedBy")
    @Mapping(target = "contacts", source = "contacts", qualifiedByName = "contactsToDtos")
    TenantResponseDto tenantToDto(Tenant tenant);


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "contacts", ignore = true)

    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdAt", expression = "java(Timestamp.from(Instant.now()))")
    @Mapping(target = "updatedAt", expression = "java(Timestamp.from(Instant.now()))")
    Tenant dtoToTenant(TenantInfoDto dto);

    // Helper methods for Timestamp <-> Instant mapping
    default Instant map(Timestamp timestamp) {
        return timestamp != null ? timestamp.toInstant() : null;
    }

    default Timestamp map(Instant instant) {
        return instant != null ? Timestamp.from(instant) : null;
    }

    ObjectMapper MAPPER = new ObjectMapper();

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "contacts", ignore = true)
    @Mapping(target = "updatedAt", expression = "java(Timestamp.from(Instant.now()))")
    @Mapping(target = "name", source = "info.name")
    @Mapping(target = "website", source = "info.website")
    @Mapping(target = "image", source = "info.image")
    @Mapping(target = "description", source = "info.description")
    @Mapping(target = "email", source = "info.email")

    void updateToTenant(TenantRequestDto dto, @MappingTarget Tenant tenant);

    @IterableMapping(qualifiedByName = "map3")
    List<Contact> dtosToContacts(List<ContactDto> dtos);

    @Named("map3")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "contactName", source = "name")
    @Mapping(target = "contactEmail", source = "email")
    @Mapping(target = "contactType", source = "type")
    @Mapping(target = "tenants", ignore = true)
    Contact dtoToContact(ContactDto dto);

    @Named("mapType")
    default ContactType mapType(String type) {
        return ContactType.valueOf(type.toUpperCase());
    }

    @Named("map4")
    default TenantRequestDto webApiTenantToTenantRequestDto(TenantWebApiGetResponse.Info info) {
        TenantRequestDto dto = new TenantRequestDto();
        TenantInfoDto dtoInfo = new TenantInfoDto();
        dtoInfo.name = info.getName();
        dtoInfo.email = info.getEmail();
        dtoInfo.website = info.getWebsite();
        dtoInfo.description = info.getDescription();
        dtoInfo.image = info.getImage();
        dtoInfo.createdAt = Instant.from(DATE_TIME_FMT.parse(info.getCreated()));
        dtoInfo.updatedAt = Instant.from(DATE_TIME_FMT.parse(info.getUpdated()));
        dto.info = dtoInfo;
        return dto;
    }

    public default Tenant mapMetadata(TenantRequestDto dto, Tenant tenant) {
        // Convert metadata object → JSON string for DB
        try {
            String metadataJson = objectMapper.writeValueAsString(dto.metadata);
            tenant.setMetadata(metadataJson);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert metadata DTO to JSON", e);
        }
        return tenant;
    }

    default TenantMetadata mapMetadataObject(String metadataJson) {
        if (metadataJson == null || metadataJson.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(metadataJson, TenantMetadata.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize metadata JSON", e);
        }
    }

    // Map TenantMetadata → String JSON
    default String mapMetadataToString(TenantMetadata metadata) {
        if (metadata == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize metadata JSON", e);
        }
    }
}