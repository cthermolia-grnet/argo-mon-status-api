package org.grnet.status.repositories;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.commons.lang3.StringUtils;
import org.grnet.status.entities.*;

import java.util.Optional;
import java.util.*;

@ApplicationScoped
public class TenantRepository implements Repository<Tenant, String> {

    public Optional<Tenant> fetchTenantByName(String name) {

        return find("from Tenant t where t.name = ?1", name).stream().findAny();
    }

    /**
     * Retrieves a page of tenants from the database.
     *
     * @param page The index of the page to retrieve (starting from 0).
     * @param size The maximum number of users to include in a page.
     * @return A list of Tenant objects representing the users in the
     * requested page.
     */
    public PageQuery<Tenant> fetchTenantsByPageAndSize(int page, int size, String search,String sort, String order) {
        var joiner = new StringJoiner(StringUtils.SPACE);
        joiner.add("from Tenant t");

        var map = new HashMap<String, Object>();

        if (StringUtils.isNotEmpty(search)) {

            joiner.add("WHERE t.name ilike :search or t.email ilike :search");
            map.put("search", "%" + search + "%");
        }
        if (StringUtils.isNotEmpty(sort)) {

            joiner.add("order by");
            joiner.add("t." + sort);
            joiner.add(order);
        } else {

            joiner.add("order by");
            joiner.add("t.name ASC");
            joiner.add(", t.createdAt DESC");
        }

        var panache = find(joiner.toString(), map).page(page, size);

        var pageable = new PageQueryImpl<Tenant>();
        pageable.list = panache.list();
        pageable.index = page;
        pageable.size = size;
        pageable.count = panache.count();
        pageable.page = Page.of(page, size);

        return pageable;

    }

    public PageQuery<Tenant> fetchTenantsByIdsAndPageAndSize(List<String> allowedIds, int page, int size, String search, String sort, String order) {

        var joiner = new StringJoiner(StringUtils.SPACE);
        joiner.add("from Tenant t WHERE t.id in :allowedIds");

        var params = new HashMap<String, Object>();
        params.put("allowedIds", allowedIds);

        if (StringUtils.isNotEmpty(search)) {
            joiner.add("AND (t.name ilike :search OR t.email ilike :search)");
            params.put("search", "%" + search + "%");
        }

        if (StringUtils.isNotEmpty(sort)) {
            joiner.add("order by t." + sort + " " + order);
        } else {
            joiner.add("order by t.name ASC, t.createdAt DESC");
        }

        var panache = find(joiner.toString(), params).page(page, size);

        var pageable = new PageQueryImpl<Tenant>();
        pageable.list = panache.list();
        pageable.index = page;
        pageable.size = size;
        pageable.count = panache.count();
        pageable.page = Page.of(page, size);

        return pageable;
    }


    /**
     * Retrieves all tenants  from the database.
     *
     * @return A list of Tenant objects representing the users in the
     * requested page.
     */
    public List<Tenant> fetchTenants() {
        return find("from Tenant t", Sort.by("createdAt").descending()).list();
    }


    public PageQueryImpl<Tenant> findByProjectId(String projectId, int page, int size, String search, String sort, String order) {

        var joiner = new StringJoiner(" ");

        joiner.add("select t from TenantProjectJunction tpj")
                .add("join tpj.tenant t")
                .add("join tpj.project p");

        var params = new HashMap<String, Object>();
        params.put("projectId", projectId);

        var where = new StringJoiner(" AND ");
        where.add("p.id = :projectId");

        if (StringUtils.isNotBlank(search)) {
            where.add("(t.name ILIKE :search OR t.email ILIKE :search)");
            params.put("search", "%" + search + "%");
        }

        joiner.add("WHERE " + where);

        if (StringUtils.isNotBlank(sort)) {
            joiner.add("ORDER BY t." + sort + " " + order);
        } else {
            joiner.add("ORDER BY t.name ASC");
        }

        var panache = find(joiner.toString(), params).page(page, size);

        var pageable = new PageQueryImpl<Tenant>();
        pageable.list = panache.list();
        pageable.index = page;
        pageable.size = size;
        pageable.count = panache.count();
        pageable.page = Page.of(page, size);

        return pageable;
    }

}