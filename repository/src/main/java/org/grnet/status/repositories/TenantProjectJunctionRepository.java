package org.grnet.status.repositories;

import jakarta.enterprise.context.ApplicationScoped;
import org.apache.commons.lang3.StringUtils;
import org.grnet.status.entities.*;

import java.util.HashMap;
import java.util.List;
import java.util.StringJoiner;

@ApplicationScoped
public class TenantProjectJunctionRepository implements Repository<TenantProjectJunction,String> {

    public List<TenantProjectJunction> findByTenantId(String tenantId) {
        return list("tenant.id", tenantId);
    }

    public List<TenantProjectJunction> findByProjectId(String projectId) {
        return list("project.id", projectId);
    }


    public PageQuery<TenantProjectJunction> fetchTenantsProjectsByPageAndSize(int page, int size, String search, String sort, String order) {
        var joiner = new StringJoiner(" ");

        joiner.add("from TenantProjectJunction tpj")
                .add("join fetch tpj.tenant t")
                .add("join fetch tpj.project p");

        var params = new HashMap<String, Object>();
        var where = new StringJoiner(" AND ");

        if (StringUtils.isNotBlank(search)) {
            where.add("t.name ilike :search OR ilike :search OR p.name ilike :search");
            params.put("search", "%" + search + "%");
        }

        if (!where.toString().isBlank()) {
            joiner.add("WHERE " + where);
        }

        if (StringUtils.isNotBlank(sort)) {
            joiner.add("ORDER BY tpj." + sort + " " + order);
        }

        var panache = find(joiner.toString(), params).page(page, size);

        var pageable = new PageQueryImpl<TenantProjectJunction>();
        pageable.list = panache.list();
        pageable.index = page;
        pageable.size = size;
        pageable.count = panache.count();
        pageable.page = Page.of(page, size);

        return pageable;
    }

    public void deleteByTenantAndProject(String tenantId, String projectId) {
        delete("tenant.id = ?1 and project.id = ?2", tenantId, projectId);
    }
}
