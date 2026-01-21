package org.grnet.status.authorizations.dtos;

import com.fasterxml.jackson.annotation.JsonGetter;
import jakarta.enterprise.inject.spi.CDI;
import org.grnet.status.authorizations.service.UserEntitlementsService;

import java.util.Collections;
import java.util.List;

public class GroupUser {

    public String id;

    public String username;
    public String firstName;
    public String lastName;

    public String email;

    public Attributes attributes;

    @JsonGetter("tenants")
    public List<UserGroupInfoDto> getTenants() {

        if (attributes != null && attributes.getLocalEntitlements() != null) {

            return CDI.current()
                    .select(UserEntitlementsService.class)
                    .get()
                    .parseLocalEntitlements(attributes.getLocalEntitlements(), "tenants");
        } else {
            return Collections.emptyList();
        }
    }
}
