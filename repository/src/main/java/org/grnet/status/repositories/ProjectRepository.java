package org.grnet.status.repositories;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.commons.lang3.StringUtils;
import org.grnet.status.entities.*;

import java.util.HashMap;
import java.util.Set;
import java.util.StringJoiner;


/**
 * Repository responsible for managing Project entities.
 */
@ApplicationScoped
public class ProjectRepository implements Repository<Project, String> {

    /**
     * Retrieves a paginated list of projects with optional search and sorting.
     *
     * @param page 0-based page index
     * @param size page size
     * @param search search filter
     * @param sort sort field
     * @param order sort order
     * @return paginated projects
     */
    public PageQuery<Project> fetchProjectByPage(int page, int size, String search, String sort, String order) {

        var joiner = new StringJoiner(" ");

        joiner.add("from Project p");

        var params = new HashMap<String, Object>();

        if (StringUtils.isNotBlank(search)) {
            joiner.add("where lower(p.name) like lower(:search)");
            params.put("search", "%" + search.trim() + "%");
        }

        var allowedSortFields = Set.of(
                "name",
                "startDate",
                "endDate",
                "sustainabilityEndDate",
                "createdAt",
                "updatedAt"
        );

        if (StringUtils.isBlank(sort) || !allowedSortFields.contains(sort)) {
            sort = "startDate"; // default
        }

        var direction = "ASC".equalsIgnoreCase(order) ? "ASC" : "DESC";

        joiner.add("order by p." + sort + " " + direction);

        PanacheQuery<Project> panache;

        if (params.isEmpty()) {
            panache = find(joiner.toString());
        } else {
            panache = find(joiner.toString(), params);
        }

        panache = panache.page(page, size);

        var result = new PageQueryImpl<Project>();
        result.list = panache.list();
        result.index = page;
        result.size = size;
        result.count = panache.count();
        result.page = Page.of(page, size);

        return result;
    }

    /**
     * Retrieves a paginated list of projects assigned to a specific tenant.
     *
     * @param tenantId tenant identifier
     * @param page 0-based page index
     * @param size page size
     * @param search search filter
     * @param sort sort field
     * @param order sort order
     * @return paginated tenant projects
     */
    public PageQueryImpl<Project> findByTenantId(String tenantId, int page, int size, String search, String sort, String order) {

        var joiner = new StringJoiner(" ");

        joiner.add("select p from TenantProjectJunction tpj")
                .add("join tpj.project p")
                .add("join tpj.tenant t");

        var params = new HashMap<String, Object>();
        params.put("tenantId", tenantId);

        var where = new StringJoiner(" AND ");
        where.add("t.id = :tenantId");

        if (StringUtils.isNotBlank(search)) {
            where.add("(p.name ILIKE :search)");
            params.put("search", "%" + search + "%");
        }

        joiner.add("WHERE " + where);

        if (StringUtils.isNotBlank(sort)) {
            joiner.add("ORDER BY p." + sort + " " + order);
        } else {
            joiner.add("ORDER BY p.name ASC");
        }

        var panache = find(joiner.toString(), params).page(page, size);

        var pageable = new PageQueryImpl<Project>();
        pageable.list = panache.list();
        pageable.index = page;
        pageable.size = size;
        pageable.count = panache.count();
        pageable.page = Page.of(page, size);

        return pageable;
    }
}
