package org.grnet.status.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;


public class ContactTenantJunction {
    @JsonProperty(value = "id", access = JsonProperty.Access.READ_ONLY)
    public String id;
    public String name;
    public String email;
    public String type;
    public List<TenantPartial> tenants;

    public ContactTenantJunction(String id, String name, String email, String type, List<TenantPartial> tenants) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.type = type;
        this.tenants = tenants;
    }

    public ContactTenantJunction(Contact contact, List<TenantPartial> tenants) {
        this.id = contact.getId();
        this.name = contact.getContactName();
        this.email = contact.getContactEmail();
        this.type = contact.getContactType().name();
        this.tenants = tenants;
    }
}

