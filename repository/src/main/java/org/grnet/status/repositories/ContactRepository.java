package org.grnet.status.repositories;

import io.quarkus.panache.common.Parameters;
import jakarta.enterprise.context.ApplicationScoped;
import org.grnet.status.entities.*;
import org.grnet.status.enums.ContactType;

import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class ContactRepository implements Repository<Contact, String> {

    public Optional<Contact> fetchContactByName(String name) {
        return find("contactName", name).firstResultOptional();
    }

    public Optional<Contact> fetchContactByInfo(String name, String email, String type) {
        return find("from Contact c where c.contactName = :name AND c.contactEmail = :email AND c.contactType = :type",
                Parameters.with("name", name).and("email", email).and("type", ContactType.valueOf(type)))
                .stream().findAny();
    }
    /**
     * Retrieves a page of tenants from the database.
     *
     * @param page The index of the page to retrieve (starting from 0).
     * @param size The maximum number of users to include in a page.
     * @return A list of Tenant objects representing the users in the
     * requested page.
     */
    public PageQuery<Contact> fetchContactsByPageAndSize(int page, int size) {
        var panache = find("from Contact c ").page(page, size);

        var pageable = new PageQueryImpl<Contact>();
        pageable.list = panache.list();
        pageable.index = page;
        pageable.size = size;
        pageable.count = panache.count();
        pageable.page = Page.of(page, size);

        return pageable;

    }

    public PageQuery<ContactTenantJunction> fetchContactsWithTenantsByPageAndSize(int page, int size) {
        var panache = find("from Contact c").page(page, size);

        var contacts = panache.list();

        // Fetch tenants for all contacts in one query
        List<String> contactIds = contacts.stream()
                .map(Contact::getId)
                .collect(Collectors.toList());

        // Query tenant id and name for contacts (using native query or JPQL)
        // JPQL: select t.id, t.name, c.id from Tenant t join t.contacts c where c.id in :contactIds
        // but JPA doesn't allow selecting entities partially easily, so do it as Object[] projection:

        List<Object[]> tenantRows = getEntityManager()
                .createQuery("select t.id, t.name, c.id from Tenant t join t.contacts c where c.id in :contactIds", Object[].class)
                .setParameter("contactIds", contactIds)
                .getResultList();

        // Map contactId -> list of TenantInfo
        Map<String, List<TenantPartial>> tenantsMap = new HashMap<>();

        for (Object[] row : tenantRows) {
            String tenantId = (String) row[0];
            String tenantName = (String) row[1];
            String contactId = (String) row[2];

            tenantsMap.computeIfAbsent(contactId, k -> new ArrayList<>())
                    .add(new TenantPartial(tenantId, tenantName));
        }

        // Build DTO list
        List<ContactTenantJunction> dtos = contacts.stream()
                .map(c -> new ContactTenantJunction(c, tenantsMap.getOrDefault(c.getId(), List.of())))
                .collect(Collectors.toList());

        var pageable = new PageQueryImpl<ContactTenantJunction>();
        pageable.list = dtos;
        pageable.index = page;
        pageable.size = size;
        pageable.count = panache.count();
        pageable.page = Page.of(page, size);

        return pageable;
    }


}