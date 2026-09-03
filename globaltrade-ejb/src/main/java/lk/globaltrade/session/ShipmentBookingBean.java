package lk.globaltrade.session;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
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
import lk.globaltrade.timer.ShipmentAlertTimerBeanLocal;

import java.util.HashSet;
import java.util.List;

@Stateless
public class ShipmentBookingBean implements ShipmentBookingBeanLocal {

    private static final double COST_PER_CONTAINER = 1000.0;
    private static final long READINESS_CHECK_DELAY_MINUTES = 30;

    @PersistenceContext(unitName = "globaltradePU")
    private EntityManager em;

    @EJB
    private ShipmentAlertTimerBeanLocal shipmentAlertTimer;

    @Override
    @RolesAllowed("CUSTOMER")
    public Shipment bookShipment(int customerId, int originPortId, int destinationPortId, int containerCount)
            throws NoContainerAvailableException {

        List<Container> reservedContainers = em.createQuery(
                        "SELECT c FROM Container c WHERE c.status = :status", Container.class)
                .setParameter("status", Container.Status.AVAILABLE)
                .setMaxResults(containerCount)
                .getResultList();

        if (reservedContainers.size() < containerCount) {
            throw new NoContainerAvailableException(
                    "Requested " + containerCount + " containers, only "
                            + reservedContainers.size() + " available");
        }

        try {
            User customer = em.find(User.class, customerId);
            Port originPort = em.find(Port.class, originPortId);
            Port destinationPort = em.find(Port.class, destinationPortId);

          if (customer == null) {
                throw new SupplyChainSystemException("Unknown customer id: " + customerId);
            }
            if (originPort == null) {
                throw new SupplyChainSystemException("Unknown origin port id: " + originPortId);
            }
            if (destinationPort == null) {
                throw new SupplyChainSystemException("Unknown destination port id: " + destinationPortId);
            }

            for (Container container : reservedContainers) {
                container.setStatus(Container.Status.RESERVED);
            }

            Shipment shipment = new Shipment();
            shipment.setCustomer(customer);
            shipment.setOriginPort(originPort);
            shipment.setDestinationPort(destinationPort);
            shipment.setStatus(Shipment.Status.PENDING);
            shipment.setEstimatedCost(COST_PER_CONTAINER * containerCount);
            shipment.setContainers(new HashSet<>(reservedContainers));

            em.persist(shipment);
            em.flush();

            shipmentAlertTimer.scheduleReadinessCheck(shipment.getId(), READINESS_CHECK_DELAY_MINUTES);

            return shipment;
        } catch (PersistenceException e) {
            throw new SupplyChainSystemException("Failed to book shipment", e);
        }
    }
}