package org.grnet.status.api.resolvers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import jakarta.ws.rs.NotFoundException;
import org.grnet.status.authorizations.filters.RequestFilter;
import org.grnet.status.authorizations.resolvers.GroupIdResolver;
import org.grnet.status.repositories.TenantRepository;

@ApplicationScoped
public class TenantNameResolver implements GroupIdResolver {

    @Inject
    TenantRepository tenantRepository;

    @Override
    public String resolve(String pathId) {

        var pathParams = RequestFilter.getPathParams();

        var tenantId = pathParams.get(pathId);

        var tenant = tenantRepository.findByIdOptional(tenantId);

        if(tenant.isPresent()){
            return tenant.get().name;
        } else {
            throw new NotFoundException("There is no Tenant with the following id: " +tenantId);
        }
    }
}
