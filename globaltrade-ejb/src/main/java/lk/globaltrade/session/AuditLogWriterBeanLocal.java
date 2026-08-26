// Path: globaltrade-ejb/src/main/java/lk/globaltrade/session/AuditLogWriterBeanLocal.java
package lk.globaltrade.session;

import jakarta.ejb.Local;
import lk.globaltrade.entities.User;

/**
 * Local business interface for {@link AuditLogWriterBean}.
 *
 * Signature is frozen by CONTRACTS.md §3 — do not add, remove, or
 * reorder parameters.
 */
@Local
public interface AuditLogWriterBeanLocal {

    /**
     * Persists one audit_logs row in its OWN transaction (REQUIRES_NEW),
     * so the row survives even if the caller's transaction later rolls
     * back. Used by AuditInterceptor (Phase 3) and ShipmentTimerBean
     * (Phase 4).
     *
     * @param user       the acting user, or {@code null} for a
     *                   system/timer-originated action
     * @param action     short verb-phrase, e.g. "CREATE_SHIPMENT",
     *                   "ACCESS_DENIED", "TIMER_STATUS_UPDATE"
     * @param entityType simple entity name, e.g. "Shipment", "User"
     * @param entityId   affected entity's id, or {@code null} when none
     *                   is available (e.g. a failed create)
     * @param details    free-text description of what happened
     */
    void writeLog(User user, String action, String entityType, Integer entityId, String details);
}