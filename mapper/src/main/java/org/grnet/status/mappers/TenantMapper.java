package org.grnet.status.mappers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.grnet.status.dtos.tenant.*;
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

    TenantMapper INSTANCE = Mappers.getMapper(TenantMapper.class);
    static final DateTimeFormatter DATE_TIME_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("UTC"));


    default List<TenantResponseDto> webApiTenantsToDtos(
            List<Tenant> tenants,
            List<TenantWebApiGetResponse> webApiGetResponses
    ) {
        List<TenantResponseDto> dtos = new ArrayList<>();

        // Build map: tenantId -> Tenant
        Map<String, Tenant> tenantMap = tenants.stream()
                .collect(Collectors.toMap(t -> t.id, t -> t));

        for (TenantWebApiGetResponse response : webApiGetResponses) {

            // Ensure response contains data
            if (response.getData() == null || response.getData().isEmpty()) {
                continue;
            }

            var item = response.getData().get(0); // id + info
            var tenant = tenantMap.get(item.getId());

            if (tenant != null) {
                dtos.add(webApiTenantToDto(tenant, item.getInfo()));
            }
        }

        return dtos;
    }

    @Named("map")
    default TenantResponseDto webApiTenantToDto(Tenant tenant, TenantWebApiGetResponse.Info info) {
        TenantResponseDto dto = new TenantResponseDto();
        dto.id = tenant.id;
        TenantWebApiRequest.TenantWebApiInfo dtoInfo = new TenantWebApiRequest.TenantWebApiInfo();
        dtoInfo.name = info.getName();
        dtoInfo.email = info.getEmail();
        dtoInfo.website = info.getWebsite();
        dtoInfo.description = info.getDescription();
        dtoInfo.image = info.getImage();
        dtoInfo.createdAt = Instant.from(DATE_TIME_FMT.parse(info.getCreated()));
        dtoInfo.updatedAt = Instant.from(DATE_TIME_FMT.parse(info.getUpdated()));
        dto.info = dtoInfo;
        dto.updatedBy = tenant.updatedBy;
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

}