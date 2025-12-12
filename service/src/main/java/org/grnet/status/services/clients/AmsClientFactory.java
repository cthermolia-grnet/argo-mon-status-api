package org.grnet.status.services.clients;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.grnet.status.services.utils.UriUtil;

/**
 * Builds instances of AmsClient for a given base URI.
 */
@ApplicationScoped
public class AmsClientFactory {

    @Inject
    UriUtil uriUtil;

    public AmsClient buildClient(String api) {
        var baseUri = uriUtil.buildUri(api);
        return RestClientBuilder.newBuilder()
                .baseUri(baseUri)
                .build(AmsClient.class);
    }
}

