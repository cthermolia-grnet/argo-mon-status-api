package org.grnet.status.api.resolvers;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotFoundException;
import org.grnet.status.authorizations.filters.RequestFilter;
import org.grnet.status.authorizations.resolvers.GroupIdResolver;
import org.grnet.status.exceptions.BadRequestException;
import org.grnet.status.repositories.TenantRepository;

@ApplicationScoped
public class TenantNameResolver implements GroupIdResolver {

    @Inject
    TenantRepository tenantRepository;

    @Override
    public String resolve(String pathId) {

        var pathParams = RequestFilter.getPathParams();
        if (pathParams == null) {
            throw new InternalServerErrorException("Request context is missing path parameters (RequestFilter not applied).");
        }

        if (!pathParams.containsKey(pathId)) {
            throw new InternalServerErrorException(
                    "Expected path parameter '" + pathId + "' is not available for this request."
            );
        }

        var tenantId = pathParams.get(pathId);

        if (tenantId == null || tenantId.isBlank()) {
            throw new BadRequestException(
                    "Missing required path parameter '" + pathId + "."
            );
        }

        return tenantRepository.findByIdOptional(tenantId)
                .map(t -> t.name)
                .orElseThrow(() -> new NotFoundException(
                        "Tenant not found for id: " + tenantId
                ));
    }
}
