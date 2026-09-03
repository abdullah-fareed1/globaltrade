package lk.globaltrade.session;

import jakarta.annotation.Resource;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ejb.SessionContext;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lk.globaltrade.entities.Container;
import lk.globaltrade.entities.Shipment;
import lk.globaltrade.entities.Ship;
import lk.globaltrade.entities.User;
import lk.globaltrade.exception.InvalidShipmentStateException;
import lk.globaltrade.exception.UnauthorizedShipmentAccessException;

import java.security.Principal;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Stateless
public class ShipmentOperationsBean implements ShipmentOperationsBeanLocal {

    private static final Map<Shipment.Status, Set<Shipment.Status>> LEGAL_TRANSITIONS = Map.of(
            Shipment.Status.PENDING, EnumSet.of(Shipment.Status.CONFIRMED, Shipment.Status.DELAYED),
            Shipment.Status.CONFIRMED, EnumSet.of(Shipment.Status.IN_TRANSIT, Shipment.Status.DELAYED),
            Shipment.Status.IN_TRANSIT, EnumSet.of(Shipment.Status.DELIVERED, Shipment.Status.DELAYED),
            Shipment.Status.DELAYED, EnumSet.of(Shipment.Status.IN_TRANSIT, Shipment.Status.DELIVERED),
            Shipment.Status.DELIVERED, EnumSet.noneOf(Shipment.Status.class)
    );

    @PersistenceContext(unitName = "globaltradePU")
    private EntityManager em;

    @Resource
    private SessionContext sessionContext;

    @EJB
    private UserAccountBeanLocal userAccountBean;

    @Override
    @RolesAllowed("COORDINATOR")
    public List<Shipment> viewActiveShipments() {
        return em.createQuery(
                        "SELECT s FROM Shipment s WHERE s.status <> :delivered", Shipment.class)
                .setParameter("delivered", Shipment.Status.DELIVERED)
                .getResultList();
    }

    @Override
    @RolesAllowed("COORDINATOR")
    public void updateStatus(int shipmentId, Shipment.Status newStatus) throws InvalidShipmentStateException {
        Shipment shipment = em.find(Shipment.class, shipmentId);
        if (shipment == null) {
            throw new InvalidShipmentStateException("Shipment " + shipmentId + " not found");
        }

        Shipment.Status current = shipment.getStatus();
        Set<Shipment.Status> allowed = LEGAL_TRANSITIONS.getOrDefault(current, EnumSet.noneOf(Shipment.Status.class));
        if (!allowed.contains(newStatus)) {
            throw new InvalidShipmentStateException(
                    "Cannot transition shipment " + shipmentId + " from " + current + " to " + newStatus);
        }

        if (newStatus == Shipment.Status.IN_TRANSIT) {
            for (Container container : shipment.getContainers()) {
                container.setStatus(Container.Status.IN_TRANSIT);
            }

            Ship assignedShip = em.createQuery(
                            "SELECT s FROM Ship s WHERE s.status = :status AND s.currentPort = :port",
                            Ship.class)
                    .setParameter("status", Ship.Status.AT_PORT)
                    .setParameter("port", shipment.getOriginPort())
                    .setMaxResults(1)
                    .getResultList()
                    .stream()
                    .findFirst()
                    .orElse(null);

            if (assignedShip == null) {
                throw new InvalidShipmentStateException(
                        "No ship available at port " + shipment.getOriginPort().getCode()
                                + " to move shipment " + shipmentId + " to IN_TRANSIT");
            }

            shipment.setShip(assignedShip);
        }

        shipment.setStatus(newStatus);
    }

    @Override
    @RolesAllowed("CUSTOMER")
    public List<Shipment> getOwnShipments() {
        User caller = currentUser();
        if (caller == null) {
            return Collections.emptyList();
        }
        return em.createQuery(
                        "SELECT s FROM Shipment s WHERE s.customer = :customer", Shipment.class)
                .setParameter("customer", caller)
                .getResultList();
    }

    @Override
    @RolesAllowed("CUSTOMER")
    public Shipment getShipmentById(int shipmentId) throws UnauthorizedShipmentAccessException {
        Shipment shipment = em.find(Shipment.class, shipmentId);
        if (shipment == null) {
            throw new UnauthorizedShipmentAccessException("Shipment " + shipmentId + " not found");
        }

        User caller = currentUser();
        if (caller == null || !shipment.getCustomer().getId().equals(caller.getId())) {
            throw new UnauthorizedShipmentAccessException(
                    "User is not authorized to access shipment " + shipmentId);
        }

        return shipment;
    }

    private User currentUser() {
        Principal principal = sessionContext.getCallerPrincipal();
        if (principal == null || "ANONYMOUS".equalsIgnoreCase(principal.getName())) {
            return null;
        }
        return userAccountBean.findByEmail(principal.getName());
    }
}