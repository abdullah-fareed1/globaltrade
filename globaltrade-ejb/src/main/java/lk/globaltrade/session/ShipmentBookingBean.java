// Path: globaltrade-ejb/src/main/java/lk/globaltrade/session/ShipmentBookingBean.java
package lk.globaltrade.session;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceException;
import lk.globaltrade.entities.Container;
import lk.globaltrade.entities.Port;
import lk.globaltrade.entities.Shipment;
import lk.globaltrade.entities.User;
import lk.globaltrade.exception.NoContainerAvailableException;
import lk.globaltrade.exception.SupplyChainSystemException;

import java.util.HashSet;
import java.util.List;

/**
 * CMT REQUIRED (container default — no @TransactionAttribute needed).
 *
 * No @Interceptors here per CONTRACTS.md §8 — Phase 3 binds the
 * Security -> Performance -> Audit chain to this class exclusively
 * through ejb-jar.xml's <interceptor-order>, not through annotations.
 *
 * Business rule is CONTRACTS.md §12 "Booking", verbatim:
 *   1. query AVAILABLE containers, capped at containerCount
 *   2. fewer found than requested -> NoContainerAvailableException
 *      (rollback=true via @ApplicationException on the exception class
 *      itself — nothing here reserves or persists after that point)
 *   3. mark each found container RESERVED
 *   4. new Shipment: PENDING, estimatedCost = 1000.0 * containerCount,
 *      eta stays null, createdAt via Shipment's own @PrePersist
 *   5. attach containers, persist
 *   6. TODO Phase 4: shipmentAlertTimer.scheduleReadinessCheck(id, 30)
 *      — ShipmentAlertTimerBean does not exist until Phase 4. Wiring
 *      the injection now would not compile; the build plan calls this
 *      TODO out explicitly rather than stubbing a placeholder bean.
 *   7. wrap PersistenceException -> SupplyChainSystemException
 */
@Stateless
public class ShipmentBookingBean implements ShipmentBookingBeanLocal {

    private static final double COST_PER_CONTAINER = 1000.0;

    @PersistenceContext(unitName = "globaltradePU")
    private EntityManager em;

    @Override
    public Shipment bookShipment(int customerId, int originPortId, int destinationPortId, int containerCount)
            throws NoContainerAvailableException {

        List<Container> reservedContainers = em.createQuery(
                        "SELECT c FROM Container c WHERE c.status = :status", Container.class)
                .setParameter("status", Container.Status.AVAILABLE)
                .setMaxResults(containerCount)
                .getResultList();

        if (reservedContainers.size() < containerCount) {
            // Thrown BEFORE any container is marked RESERVED and before
            // anything is persisted — step 2 happens ahead of step 3.
            throw new NoContainerAvailableException(
                    "Requested " + containerCount + " containers, only "
                            + reservedContainers.size() + " available");
        }

        try {
            for (Container container : reservedContainers) {
                container.setStatus(Container.Status.RESERVED);
            }

            User customer = em.find(User.class, customerId);
            Port originPort = em.find(Port.class, originPortId);
            Port destinationPort = em.find(Port.class, destinationPortId);

            Shipment shipment = new Shipment();
            shipment.setCustomer(customer);
            shipment.setOriginPort(originPort);
            shipment.setDestinationPort(destinationPort);
            shipment.setStatus(Shipment.Status.PENDING);
            shipment.setEstimatedCost(COST_PER_CONTAINER * containerCount);
            // eta intentionally left null until a coordinator confirms.
            shipment.setContainers(new HashSet<>(reservedContainers));

            em.persist(shipment);
            // createdAt populated by Shipment.onCreate() @PrePersist.

            // TODO Phase 4: shipmentAlertTimer.scheduleReadinessCheck(shipment.getId(), 30);

            return shipment;
        } catch (PersistenceException e) {
            // System exception (unchecked, no @ApplicationException) ->
            // per CONTRACTS.md §10 the container wraps this in
            // EJBException before it reaches any caller.
            throw new SupplyChainSystemException("Failed to book shipment", e);
        }
    }
}