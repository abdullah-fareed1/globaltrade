// Path: globaltrade-ejb/src/main/java/lk/globaltrade/interceptor/SecurityInterceptor.java
package lk.globaltrade.interceptor;

import jakarta.annotation.Resource;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ejb.EJBAccessException;
import jakarta.ejb.SessionContext;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.InvocationContext;
import lk.globaltrade.session.AuditLogWriterBeanLocal;

import java.util.Arrays;

/**
 * Declarative programmatic security check, CONTRACTS.md §6.
 *
 * Bound in ejb-jar.xml only (not via @Interceptors) to
 * ShipmentBookingBean / ShipmentOperationsBean, first in the
 * Security -> Performance -> Audit order, so an unauthorised call is
 * rejected before it is ever timed or audited as a business outcome.
 *
 * Deliberately uses the standard jakarta.annotation.security.RolesAllowed
 * rather than a custom annotation. Method-level @RolesAllowed beats
 * class-level; if neither is present, the call is allowed through
 * unrestricted.
 */
public class SecurityInterceptor {

    @Resource
    private SessionContext sessionContext;

    @EJB
    private AuditLogWriterBeanLocal auditLogWriter;

    @AroundInvoke
    public Object authorize(InvocationContext ctx) throws Exception {
        RolesAllowed rolesAllowed = ctx.getMethod().getAnnotation(RolesAllowed.class);
        if (rolesAllowed == null) {
            rolesAllowed = ctx.getTarget().getClass().getAnnotation(RolesAllowed.class);
        }

        if (rolesAllowed != null) {
            boolean authorized = Arrays.stream(rolesAllowed.value())
                    .anyMatch(sessionContext::isCallerInRole);

            if (!authorized) {
                auditLogWriter.writeLog(null, "ACCESS_DENIED",
                        ctx.getTarget().getClass().getSimpleName(), null,
                        "Denied " + ctx.getMethod().getName()
                                + " for " + sessionContext.getCallerPrincipal().getName());
                throw new EJBAccessException("Not authorised: " + ctx.getMethod().getName());
            }
        }

        return ctx.proceed(); // no @RolesAllowed present => allow
    }
}