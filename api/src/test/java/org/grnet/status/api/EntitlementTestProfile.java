package org.grnet.status.api;


import io.quarkus.test.junit.QuarkusTestProfile;

import java.util.Set;

public class EntitlementTestProfile implements QuarkusTestProfile {

    @Override
    public Set<Class<?>> getEnabledAlternatives() {
        return Set.of(
                TestEntitlementProvider.class,
                TestRoleEndpointRepository.class
        );
    }
}
