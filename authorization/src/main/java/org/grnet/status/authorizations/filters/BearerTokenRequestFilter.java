package org.grnet.status.authorizations.filters;

import jakarta.inject.Inject;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import org.grnet.status.authorizations.groups.AuthGroupManagement;

public class BearerTokenRequestFilter implements ClientRequestFilter {

    @Inject
    AuthGroupManagement groupManagement;

    @Override
    public void filter(ClientRequestContext requestContext) {
        var token = groupManagement.getAccessToken();
        requestContext.getHeaders().putSingle("Authorization", "Bearer " + token);
    }
}
