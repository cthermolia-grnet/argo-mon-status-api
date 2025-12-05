package org.grnet.status.api.resolvers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.grnet.status.authorizations.resolvers.GroupIdResolver;
import org.grnet.status.repositories.TenantRepository;

@ApplicationScoped
public class TenantNameResolver implements GroupIdResolver {

    @Inject
    TenantRepository tenantRepository;

    @Override
    public String resolve(String subgroupValue) {

        var tenantOpt = tenantRepository.fetchTenantByName(subgroupValue);

        return tenantOpt.map(tenant -> tenant.id).orElse(null);

    }
}
