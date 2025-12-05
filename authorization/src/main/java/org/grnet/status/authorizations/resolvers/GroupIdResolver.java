package org.grnet.status.authorizations.resolvers;

public interface GroupIdResolver {
    /**
     * Convert a subgroup value (e.g., slug, name, code)
     * into the internal ID used for authorization checks.
     */
    String resolve(String subgroupValue);
}
