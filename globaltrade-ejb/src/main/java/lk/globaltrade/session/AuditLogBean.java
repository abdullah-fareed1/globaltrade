// Path: globaltrade-ejb/src/main/java/lk/globaltrade/session/AuditLogBean.java
package lk.globaltrade.session;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lk.globaltrade.entities.AuditLog;

import java.util.List;

/**
 * GAP FILL — Phase 6. Read-only accessor for AdminAuditLogServlet.
 * CMT with the container default (REQUIRED) — no @TransactionAttribute
 * needed for a single SELECT.
 *
 * Deliberately un-intercepted, same reasoning as ContainerBean / ShipBean
 * / UserAccountBean (CONTRACTS.md §8): the Security -> Performance ->
 * Audit chain is bound in ejb-jar.xml only to ShipmentBookingBean and
 * ShipmentOperationsBean. This class must NOT carry an @Interceptors
 * annotation and must not appear in ejb-jar.xml's <interceptor-order>
 * list. (It would also be a little strange to audit reads of the audit
 * log itself.)
 */
@Stateless
public class AuditLogBean implements AuditLogBeanLocal {

    @PersistenceContext(unitName = "globaltradePU")
    private EntityManager em;

    @Override
    public List<AuditLog> findAllNewestFirst() {
        return em.createQuery(
                        "SELECT a FROM AuditLog a ORDER BY a.createdAt DESC, a.id DESC", AuditLog.class)
                .getResultList();
    }
}
