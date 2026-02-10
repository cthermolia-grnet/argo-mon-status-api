package org.grnet.status.authorizations.interceptors;

import jakarta.annotation.Priority;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.ws.rs.ForbiddenException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.grnet.status.authorizations.service.AccessControlService;
import org.grnet.status.authorizations.filters.RequestFilter;
import org.grnet.status.authorizations.resolvers.GroupIdResolver;
import org.grnet.status.authorizations.resolvers.NoOpResolver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@CheckEntitlements
@Interceptor
@Priority(Interceptor.Priority.APPLICATION)
public class CheckEntitlementsInterceptor {

    @Inject
    AccessControlService accessControlService;

    @Inject
    Instance<GroupIdResolver> resolverInstances;

    @ConfigProperty(name = "api.auth.entitlements.parent.group")
    String parentGroup;


    @AroundInvoke
    public Object check(InvocationContext ctx) throws Exception {

        var methodAnn = ctx.getMethod().getAnnotation(CheckEntitlements.class);
        var classAnn  = ctx.getTarget().getClass().getAnnotation(CheckEntitlements.class);

        if (methodAnn == null && classAnn == null) {
            return ctx.proceed();
        }

        if(methodAnn != null && methodAnn.byPassAuthorization()){
            return ctx.proceed();
        }

        var group = (classAnn != null && !classAnn.group().isBlank())
                ? classAnn.group()
                : "";

        var roles = methodAnn != null ? methodAnn.roles() : new String[0];

        var isSuperAdmin = accessControlService.isSuperAdmin();

        // Global bypass
        // - class-level defines it
        // - method-level can only make it stricter (false)
        var classBypass = (classAnn == null) || classAnn.superAdminBypass();
        var methodDisablesBypass = (methodAnn != null) && !methodAnn.superAdminBypass();
        var effectiveBypass = methodDisablesBypass ? false : classBypass;

        if (isSuperAdmin && effectiveBypass) {
            return ctx.proceed();
        }

        var groups = new ArrayList<String>();
        groups.add(parentGroup);
        groups.add(group);

        if(methodAnn != null){

            var resolvers = methodAnn.resolvers();

            for(int i= 0; i<resolvers.length; i++){

                var resolverClass = resolvers[i].idResolver();

                var actualResolver = resolverInstances.select(resolverClass).get();

                var pathValue = actualResolver.resolve(resolvers[i].pathId());

                groups.add(pathValue);
            }
        }

        for(String role:roles){

            if(accessControlService.hasAccess(role, groups.stream().filter(s -> s != null && !s.isBlank()).collect(Collectors.toList()), group)){

                return ctx.proceed();
            }
        }

        throw new ForbiddenException("Access denied.");
    }
}