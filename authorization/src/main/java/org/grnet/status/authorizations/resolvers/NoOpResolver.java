package org.grnet.status.authorizations.resolvers;

public class NoOpResolver implements GroupIdResolver {
    @Override
    public String resolve(String subgroupValue) {
        return subgroupValue;
    }
}
