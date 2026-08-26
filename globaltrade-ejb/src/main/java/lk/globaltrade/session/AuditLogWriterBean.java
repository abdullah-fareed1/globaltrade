// Path: globaltrade-ejb/src/main/java/lk/globaltrade/session/AuditLogWriterBean.java
package lk.globaltrade.session;

import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lk.globaltrade.entities.AuditLog;
import lk.globaltrade.entities.User;

/**
 * Writes one audit_logs row per call, always in its own, brand-new
 * transaction (CONTRACTS.md §9 — REQUIRES_NEW is frozen for this bean).
 *
 * This is what makes "the audit trail survives a rolled-back booking"
 * possible: AuditInterceptor calls writeLog() from inside its catch
 * block when ctx.proceed() throws. Because this bean's transaction is
 * REQUIRES_NEW rather than the default REQUIRED, the audit row commits
 * on its own even though the caller's (Security -> Performance -> Audit
 * -> business method) transaction is about to roll back around it.
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class AuditLogWriterBean implements AuditLogWriterBeanLocal {

    @PersistenceContext(unitName = "globaltradePU")
    private EntityManager em;

    @Override
    public void writeLog(User user, String action, String entityType, Integer entityId, String details) {
        AuditLog log = new AuditLog();
        log.setUser(user);
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setDetails(details);
        // createdAt is populated by AuditLog's own @PrePersist hook.
        em.persist(log);
    }
}