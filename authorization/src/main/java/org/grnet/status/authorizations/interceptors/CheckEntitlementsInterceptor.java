package org.grnet.status.authorizations.interceptors;

import jakarta.annotation.Priority;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.ws.rs.ForbiddenException;
import org.grnet.status.authorizations.service.AccessControlService;
import org.grnet.status.authorizations.filters.RequestFilter;
import org.grnet.status.authorizations.resolvers.GroupIdResolver;
import org.grnet.status.authorizations.resolvers.NoOpResolver;

@CheckEntitlements
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
public class CheckEntitlementsInterceptor {

    @Inject
    AccessControlService accessControlService;

    @Inject
    Instance<GroupIdResolver> resolverInstances;

    @AroundInvoke
    public Object check(InvocationContext ctx) throws Exception {

        var methodAnn = ctx.getMethod().getAnnotation(CheckEntitlements.class);
        var classAnn  = ctx.getTarget().getClass().getAnnotation(CheckEntitlements.class);

        if (methodAnn == null && classAnn == null) {
            return ctx.proceed();
        }

        var group = (classAnn != null && !classAnn.group().isBlank())
                ? classAnn.group()
                : "";

        var role = (methodAnn != null && !methodAnn.role().isBlank())
                ? methodAnn.role()
                : "";

        var requireSuperAdmin =
                (classAnn != null && classAnn.requireSuperAdmin()) ||
                        (methodAnn != null && methodAnn.requireSuperAdmin());

        var isSuperAdmin = accessControlService.isSuperAdmin();

        // Explicit requirement
        if (requireSuperAdmin && !isSuperAdmin) {
            throw new ForbiddenException("Access denied — super admin privileges required.");
        }

        // Global bypass
        if (isSuperAdmin) {
            return ctx.proceed();
        }

        // Determine resolver (method override > class resolver > default)
        Class<? extends GroupIdResolver> resolverClass =
                (methodAnn != null && methodAnn.idResolver() != NoOpResolver.class)
                        ? methodAnn.idResolver()
                        : (classAnn != null && classAnn.idResolver() != NoOpResolver.class)
                        ? classAnn.idResolver()
                        : NoOpResolver.class;

        GroupIdResolver resolver = resolverInstances.select(resolverClass).get();

        // Extract path ID (e.g. tenantId from URL)
        var pathParams = RequestFilter.getPathParams();
        String pathId = null;

        if (pathParams != null && !pathParams.isEmpty()) {
            pathId = pathParams.values().iterator().next();
        }

        boolean allowed = accessControlService.hasAccess(group, role, pathId, resolver);

        if (!allowed) {
            throw new ForbiddenException("Access denied.");
        }

        return ctx.proceed();
    }
}