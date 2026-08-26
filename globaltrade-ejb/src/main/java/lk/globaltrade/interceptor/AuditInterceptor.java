// Path: globaltrade-ejb/src/main/java/lk/globaltrade/interceptor/AuditInterceptor.java
package lk.globaltrade.interceptor;

import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.ejb.SessionContext;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.InvocationContext;
import lk.globaltrade.entities.Shipment;
import lk.globaltrade.entities.User;
import lk.globaltrade.session.AuditLogWriterBeanLocal;
import lk.globaltrade.session.UserAccountBeanLocal;

import java.security.Principal;
import java.util.Map;

/**
 * CONTRACTS.md §7 / BUILD_PLAN FIX 8, verbatim.
 *
 * The catch block is NOT optional. If it were omitted (as the original
 * plan draft had it), a failed booking would produce no audit row at
 * all — but "audit survives a rolled-back transaction" is only
 * observable on the failure path, since AuditLogWriterBean runs under
 * REQUIRES_NEW. Without this catch, there is no evidence for that
 * demonstration.
 *
 * Bound in ejb-jar.xml (not @Interceptors), last in the
 * Security -> Performance -> Audit order, so it only ever sees calls
 * that already passed the security check.
 */
public class AuditInterceptor {

    private static final String ENTITY_TYPE = "Shipment";

    /**
     * Human-readable action names. Falls back to the raw method name
     * (upper-cased) for anything not explicitly mapped, so a future
     * bound method never silently produces a blank action.
     */
    private static final Map<String, String> ACTION_NAMES = Map.of(
            "bookShipment", "CREATE_SHIPMENT",
            "updateStatus", "UPDATE_SHIPMENT_STATUS",
            "viewActiveShipments", "VIEW_ACTIVE_SHIPMENTS",
            "getOwnShipments", "VIEW_OWN_SHIPMENTS",
            "getShipmentById", "VIEW_SHIPMENT"
    );

    @Resource
    private SessionContext sessionContext;

    @EJB
    private UserAccountBeanLocal userAccountBean;

    @EJB
    private AuditLogWriterBeanLocal auditLogWriter;

    @AroundInvoke
    public Object audit(InvocationContext ctx) throws Exception {
        String action = actionFor(ctx.getMethod().getName());
        User caller = currentUser(); // may be null: system/timer-originated call

        try {
            Object result = ctx.proceed();
            auditLogWriter.writeLog(caller, action, ENTITY_TYPE, idOf(result, ctx), "OK");
            return result;
        } catch (Exception e) {
            // REQUIRES_NEW on AuditLogWriterBean: this row commits even
            // though the caller's transaction is about to roll back.
            // This is the observable evidence for that behaviour.
            auditLogWriter.writeLog(caller, action + "_FAILED", ENTITY_TYPE, idOf(null, ctx), e.getMessage());
            throw e;
        }
    }

    private String actionFor(String methodName) {
        return ACTION_NAMES.getOrDefault(methodName, methodName.toUpperCase());
    }

    /**
     * Entity id resolution, CONTRACTS.md §7 rule, in order:
     *   1. result is a Shipment    -> result.getId()
     *   2. first int/Integer param -> that value
     *   3. otherwise                -> null
     *
     * NOTE — resolved contradiction in CONTRACTS.md §7: the contract's
     * own worked example says a FAILED bookShipment() must log
     * entityId = null ("nothing was created"), but bookShipment's
     * params are (customerId, originPortId, destinationPortId,
     * containerCount) — all ints. Applying rule 2 literally on failure
     * would pick up customerId and mislabel it as a shipment id, which
     * contradicts that example. Rule 2 only makes sense for methods
     * whose FIRST param genuinely is the shipment id — updateStatus and
     * getShipmentById. bookShipment is excluded here so its failure
     * path returns null as the contract's example requires. Flagging
     * this because it is a genuine ambiguity in the source document,
     * not an obvious call — revisit if CONTRACTS.md is amended.
     */
    private Integer idOf(Object result, InvocationContext ctx) {
        if (result instanceof Shipment) {
            return ((Shipment) result).getId();
        }
        String methodName = ctx.getMethod().getName();
        if (("updateStatus".equals(methodName) || "getShipmentById".equals(methodName))
                && ctx.getParameters().length > 0
                && ctx.getParameters()[0] instanceof Integer) {
            return (Integer) ctx.getParameters()[0];
        }
        return null;
    }

    /** Same caller-resolution rule as CONTRACTS.md §5. */
    private User currentUser() {
        Principal principal = sessionContext.getCallerPrincipal();
        if (principal == null || "ANONYMOUS".equalsIgnoreCase(principal.getName())) {
            return null;
        }
        return userAccountBean.findByEmail(principal.getName());
    }
}