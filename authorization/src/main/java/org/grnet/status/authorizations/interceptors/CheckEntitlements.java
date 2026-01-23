package org.grnet.status.authorizations.interceptors;

import jakarta.interceptor.InterceptorBinding;
import jakarta.enterprise.util.Nonbinding;

import java.lang.annotation.*;

@InterceptorBinding
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface CheckEntitlements {

    @Nonbinding
    boolean superAdminBypass() default true;

    @Nonbinding
    String group() default "";

    @Nonbinding
    String role() default "";

    @Nonbinding
    Resolver[] resolvers() default {};

    @Nonbinding
    String[] hierarchy() default {};

    @Nonbinding
    String pathParam() default "";

    @Nonbinding
    boolean byPassAuthorization() default false;
}
