// Path: globaltrade-ejb/src/main/java/lk/globaltrade/session/ShipBean.java
package lk.globaltrade.session;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lk.globaltrade.entities.Port;
import lk.globaltrade.entities.Ship;

import java.util.List;

/**
 * Admin CRUD bean for ships. CMT with the container default (REQUIRED)
 * — no @TransactionAttribute needed.
 *
 * Deliberately un-intercepted (CONTRACTS.md §8) — same reasoning as
 * ContainerBean: no @Interceptors here, and this class must not appear
 * in the ejb-jar.xml <interceptor-order> list.
 *
 * Deliberately exposes no path to edit currentPort: per ENTITIES.md,
 * that field is written exclusively by ShipmentTimerBean (Phase 4). Both
 * create() (initial placement only) and updateStatus() respect that
 * write-ownership boundary.
 */
@Stateless
public class ShipBean implements ShipBeanLocal {

    @PersistenceContext(unitName = "globaltradePU")
    private EntityManager em;

    @Override
    public List<Ship> findAll() {
        return em.createQuery("SELECT s FROM Ship s", Ship.class)
                .getResultList();
    }

    @Override
    public Ship create(String name, int capacity, int initialPortId) {
        Port initialPort = em.find(Port.class, initialPortId);
        Ship ship = new Ship(name, capacity, Ship.Status.AT_PORT, initialPort);
        em.persist(ship);
        return ship;
    }

    @Override
    public void updateStatus(int shipId, Ship.Status newStatus) {
        Ship ship = em.find(Ship.class, shipId);
        if (ship != null) {
            ship.setStatus(newStatus);
        }
    }
}