package org.grnet.status.authorizations.filters;

import jakarta.inject.Inject;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import org.grnet.status.authorizations.tokens.AccessTokenProvider;

public class BearerTokenRequestFilter implements ClientRequestFilter {

    @Inject
    AccessTokenProvider accessTokenProvider;

    @Override
    public void filter(ClientRequestContext requestContext) {
        var token = accessTokenProvider.getAccessToken();
        requestContext.getHeaders().putSingle("Authorization", "Bearer " + token);
    }
}
