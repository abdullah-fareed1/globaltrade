// Path: globaltrade-ejb/src/main/java/lk/globaltrade/session/PortBean.java
package lk.globaltrade.session;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lk.globaltrade.entities.Port;

import java.util.List;

/**
 * GAP FILL — Phase 6. See {@link PortBeanLocal}.
 *
 * Deliberately un-intercepted, same reasoning as ContainerBean / ShipBean
 * / UserAccountBean / AuditLogBean (CONTRACTS.md Sec8): not part of the
 * Security -> Performance -> Audit chain, and must not appear in
 * ejb-jar.xml's <interceptor-order>.
 */
@Stateless
public class PortBean implements PortBeanLocal {

    @PersistenceContext(unitName = "globaltradePU")
    private EntityManager em;

    @Override
    public List<Port> findAll() {
        return em.createQuery("SELECT p FROM Port p ORDER BY p.name", Port.class)
                .getResultList();
    }
}
