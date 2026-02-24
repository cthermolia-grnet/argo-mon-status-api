package org.grnet.status.services.utils;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.BadRequestException;

import java.net.URI;
import java.net.URISyntaxException;

@ApplicationScoped
public class UriUtil {

    public URI buildUri(String apiUrl) {
        try {
            var uri = new URI(apiUrl);
            if (uri.getScheme() == null || uri.getHost() == null) {
                throw new BadRequestException("URL: "+apiUrl+" must include scheme and host (e.g. http://example.com)");
            }
            return uri;
        } catch (URISyntaxException e) {
            throw new BadRequestException("The URL: "+ apiUrl+" is not correctly formatted.");
        }
    }
}
