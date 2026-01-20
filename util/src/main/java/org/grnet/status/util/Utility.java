package org.grnet.status.util;

import io.quarkus.oidc.TokenIntrospection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static java.lang.Math.min;
import static java.util.stream.Collectors.toMap;

@ApplicationScoped
public class Utility {

    /**
     * Injection point for the Token Introspection
     */
    @Inject
    TokenIntrospection tokenIntrospection;

    @ConfigProperty(name = "api.oidc.user.unique.id")
    String key;

    public String getUserUniqueIdentifier() {

        String id;

        try {
            id = tokenIntrospection.getJsonObject().getString(key);
        } catch (Exception e) {

            String message = String.format("The User's unique identifier {%s} is missing from the access token.", key);
            throw new BadRequestException(message);
        }

        return id;
    }

    public String getUsername() {
        try {
            return tokenIntrospection.getJsonObject().getString("preferred_username");
        } catch (Exception e) {
            throw new BadRequestException("Missing 'preferred_username' in access token.");
        }
    }

    public String getUserEmail() {
        return tokenIntrospection.getJsonObject().getString("email", null);
    }

    public String getUserName() {
        return tokenIntrospection.getJsonObject().getString("given_name", null);
    }

    public String getUserSurname() {
        return tokenIntrospection.getJsonObject().getString("family_name", null);
    }

    /**
     * This method paginates a list of objects.
     *
     * @param list The list to be paginated.
     * @param pageSize The page size.
     * @return A map containing the pages of objects.
     */
    public <T> Map<Integer, List<T>> partition(List<T> list, int pageSize) {

        return IntStream.iterate(0, i -> i + pageSize)
                .limit((list.size() + pageSize - 1) / pageSize)
                .boxed()
                .collect(toMap(i -> i / pageSize,
                        i -> list.subList(i, min(i + pageSize, list.size()))));
    }
}
