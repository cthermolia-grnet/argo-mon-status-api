package org.grnet.status.repositories;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.commons.lang3.StringUtils;
import org.grnet.status.entities.*;
import org.grnet.status.enums.InvitationStatus;

import java.util.HashMap;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;


@ApplicationScoped
public class TenantInvitationRepository implements Repository<TenantInvitation, String> {

    public Optional<TenantInvitation> findPendingInvitationsByTenantAndEmail(String tenantId, String email) {
        return find("tenant.id = ?1 and lower(email) = ?2 and status  = ?3",
                tenantId, email.toLowerCase(), InvitationStatus.PENDING)
                .firstResultOptional();
    }
    public PageQuery<TenantInvitation> findAllByEmail(String email, int page, int size) {

        var panache = find("email = ?1", Sort.by("createdAt", Sort.Direction.Descending), email).page(page, size);

        var pageable = new PageQueryImpl<TenantInvitation>();
        pageable.list = panache.list();
        pageable.index = page;
        pageable.size = size;
        pageable.count = panache.count();
        pageable.page = Page.of(page, size);

        return pageable;

    }

    public PageQuery<TenantInvitation> fetchInvitationsByPageAndSize(String search, String sort, String order, int page, int size) {

        var joiner = new StringJoiner(" ");

        joiner.add("SELECT ti from TenantInvitation ti")
                .add("left join ti.tenant t");

        var params = new HashMap<String, Object>();

        if (StringUtils.isNotBlank(search)) {
            joiner.add("where (t.name ILIKE :search OR ti.username ILIKE :search OR ti.email ILIKE :search)");
            params.put("search", "%" + search.trim() + "%");
        }

        var allowedSortFields = Set.of(
                "createdAt",
                "email",
                "username",
                "name",
                "status"
        );

        if (StringUtils.isBlank(sort) || !allowedSortFields.contains(sort)) {
            sort = "createdAt";
        }

        var direction = "ASC".equalsIgnoreCase(order) ? "ASC" : "DESC";

        var orderByField = "name".equals(sort) ? "t.name" : "ti." + sort;

        joiner.add("order by " + orderByField + " " + direction);

        var panache = find(joiner.toString(), params).page(page, size);

        var result = new PageQueryImpl<TenantInvitation>();
        result.list = panache.list();
        result.index = page;
        result.size = size;
        result.count = panache.count();
        result.page = Page.of(page, size);

        return result;
    }

    public PageQuery<TenantInvitation> fetchTenantInvitationsByPageAndSize(String search, String sort, String order, String tenantId, int page, int size) {

        var joiner = new StringJoiner(" ");

        joiner.add("SELECT ti from TenantInvitation ti");

        var params = new HashMap<String, Object>();

        if (StringUtils.isNotBlank(search)) {
            joiner.add("where (ti.email ILIKE :search)");
            params.put("search", "%" + search.trim() + "%");
        }

        var allowedSortFields = Set.of(
                "createdAt",
                "email",
                "status"
        );

        if (StringUtils.isBlank(sort) || !allowedSortFields.contains(sort)) {
            sort = "createdAt";
        }

        var direction = "ASC".equalsIgnoreCase(order) ? "ASC" : "DESC";

        var orderByField = "name".equals(sort) ? "t.name" : "ti." + sort;

        joiner.add("order by " + orderByField + " " + direction);

        var panache = find(joiner.toString(), params).page(page, size);

        var result = new PageQueryImpl<TenantInvitation>();
        result.list = panache.list();
        result.index = page;
        result.size = size;
        result.count = panache.count();
        result.page = Page.of(page, size);

        return result;
    }
}
