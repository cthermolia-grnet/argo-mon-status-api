package org.grnet.status.authorizations.interceptors;

import jakarta.interceptor.InterceptorBinding;
import jakarta.enterprise.util.Nonbinding;
import org.grnet.status.authorizations.resolvers.GroupIdResolver;
import org.grnet.status.authorizations.resolvers.NoOpResolver;

import java.lang.annotation.*;

@InterceptorBinding
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface CheckEntitlements {

    @Nonbinding
    boolean requireSuperAdmin() default false;

    @Nonbinding
    String group() default "";

    @Nonbinding
    String role() default "";

    @Nonbinding
    Class<? extends GroupIdResolver> idResolver() default NoOpResolver.class;

    @Nonbinding
    String[] hierarchy() default {};

    @Nonbinding
    String pathParam() default "";
}
