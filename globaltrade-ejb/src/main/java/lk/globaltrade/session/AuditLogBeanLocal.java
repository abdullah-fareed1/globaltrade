// Path: globaltrade-ejb/src/main/java/lk/globaltrade/session/AuditLogBeanLocal.java
package lk.globaltrade.session;

import jakarta.ejb.Local;
import lk.globaltrade.entities.AuditLog;

import java.util.List;

/**
 * Local business interface for {@link AuditLogBean}.
 *
 * GAP FILL — Phase 6. CONTRACTS.md §3 declares this interface ("used by
 * AdminAuditLogServlet") but no phase agent ever created the file. It is
 * added now, at the point the web layer actually needs it, following the
 * same shape as every other read-only Local interface in this package.
 *
 * Signature is frozen going forward — do not add, remove, or reorder
 * parameters.
 */
@Local
public interface AuditLogBeanLocal {

    /**
     * All audit log rows, most recent first. Backs
     * {@code /admin/auditLog}. {@code user == null} on a row means a
     * system/timer action (see ENTITIES.md §6) — the servlet/JSP layer
     * renders that as "SYSTEM".
     */
    List<AuditLog> findAllNewestFirst();
}
