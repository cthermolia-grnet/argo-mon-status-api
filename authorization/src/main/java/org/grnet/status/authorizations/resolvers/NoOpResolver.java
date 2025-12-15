package org.grnet.status.authorizations.resolvers;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class NoOpResolver implements GroupIdResolver {
    @Override
    public String resolve(String subgroupValue) {
        return subgroupValue;
    }
}
