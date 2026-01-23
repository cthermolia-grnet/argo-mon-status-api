package org.grnet.status.authorizations.interceptors;

import org.grnet.status.authorizations.resolvers.GroupIdResolver;
import org.grnet.status.authorizations.resolvers.NoOpResolver;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface Resolver {
    Class<? extends GroupIdResolver> idResolver() default NoOpResolver.class;
    String pathId() default "";
}
