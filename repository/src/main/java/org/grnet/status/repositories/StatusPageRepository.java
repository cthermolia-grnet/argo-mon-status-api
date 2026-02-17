package org.grnet.status.repositories;

import io.quarkus.panache.common.Sort;
import jakarta.enterprise.context.ApplicationScoped;
import org.grnet.status.entities.Page;
import org.grnet.status.entities.PageQuery;
import org.grnet.status.entities.PageQueryImpl;
import org.grnet.status.entities.StatusPage;

import java.util.List;


@ApplicationScoped
public class StatusPageRepository implements Repository<StatusPage, String> {

    public PageQuery<StatusPage> fetchStatusPageByTenantAndAndUserAndPage(int page, int size, String tenantId, String userId){


        var panache = find("from StatusPage sp where sp.tenant.id = ?1 and sp.userId = ?2", Sort.by("userId", Sort.Direction.Descending), tenantId, userId).page(page, size);

        var pageable = new PageQueryImpl<StatusPage>();
        pageable.list = panache.list();
        pageable.index = page;
        pageable.size = size;
        pageable.count = panache.count();
        pageable.page = Page.of(page, size);

        return pageable;
    }

    public PageQuery<StatusPage> fetchStatusPagesByTenant(int page, int size, String tenantId) {

        var panache = find(
                "from StatusPage sp where sp.tenant.id = ?1", Sort.by("createdAt", Sort.Direction.Descending), tenantId).page(page, size);

        var pageable = new PageQueryImpl<StatusPage>();
        pageable.list = panache.list();
        pageable.index = page;
        pageable.size = size;
        pageable.count = panache.count();
        pageable.page = Page.of(page, size);

        return pageable;
    }

    public PageQuery<StatusPage> fetchStatusPageByPage(int page, int size){

        var panache = find("from StatusPage sp", Sort.by("createdAt", Sort.Direction.Descending)).page(page, size);

        var pageable = new PageQueryImpl<StatusPage>();
        pageable.list = panache.list();
        pageable.index = page;
        pageable.size = size;
        pageable.count = panache.count();
        pageable.page = Page.of(page, size);

        return pageable;
    }


    public List<StatusPage> listByTenant(String tenantId) {
        return find("tenant.id = ?1", Sort.by("createdAt", Sort.Direction.Descending), tenantId).list();
    }

    public List<StatusPage> listByTenantAndUser(String tenantId, String userId) {
        return find("tenant.id = ?1 and userId = ?2",
                Sort.by("createdAt", Sort.Direction.Descending),
                tenantId, userId).list();
    }
}
