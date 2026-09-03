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

public class AuditInterceptor {

    private static final String ENTITY_TYPE = "Shipment";

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
        User caller = currentUser();

        try {
            Object result = ctx.proceed();
            auditLogWriter.writeLog(caller, action, ENTITY_TYPE, idOf(result, ctx), "OK");
            return result;
        } catch (Exception e) {
            auditLogWriter.writeLog(caller, action + "_FAILED", ENTITY_TYPE, idOf(null, ctx), e.getMessage());
            throw e;
        }
    }

    private String actionFor(String methodName) {
        return ACTION_NAMES.getOrDefault(methodName, methodName.toUpperCase());
    }

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

    private User currentUser() {
        Principal principal = sessionContext.getCallerPrincipal();
        if (principal == null || "ANONYMOUS".equalsIgnoreCase(principal.getName())) {
            return null;
        }
        return userAccountBean.findByEmail(principal.getName());
    }
}