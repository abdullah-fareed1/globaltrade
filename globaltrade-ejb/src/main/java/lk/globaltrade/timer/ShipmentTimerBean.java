package lk.globaltrade.timer;

import jakarta.ejb.EJB;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.time.LocalDate;
import java.util.List;

import lk.globaltrade.entities.Container;
import lk.globaltrade.entities.Ship;
import lk.globaltrade.entities.Shipment;
import lk.globaltrade.session.AuditLogWriterBeanLocal;

@Singleton
@Startup
public class ShipmentTimerBean {

    @PersistenceContext(unitName = "globaltradePU")
    private EntityManager em;

    @EJB
    private AuditLogWriterBeanLocal auditLogWriter;

    @Schedule(minute = "*/15", hour = "*", persistent = false)
    public void advanceShipments() {

        List<Shipment> due = em.createQuery(
                        "SELECT s FROM Shipment s "
                                + "WHERE s.status = :status AND s.eta <= :today",
                        Shipment.class)
                .setParameter("status", Shipment.Status.IN_TRANSIT)
                .setParameter("today", LocalDate.now())
                .getResultList();

        for (Shipment shipment : due) {

            shipment.setStatus(Shipment.Status.DELIVERED);

            for (Container container : shipment.getContainers()) {
                container.setStatus(Container.Status.AVAILABLE);
            }

            Ship ship = shipment.getShip();
            if (ship != null) {
                ship.setCurrentPort(shipment.getDestinationPort());
                ship.setStatus(Ship.Status.AT_PORT);
            }
            auditLogWriter.writeLog(null, "TIMER_STATUS_UPDATE", "Shipment",
                    shipment.getId(),
                    "Shipment " + shipment.getId()
                            + " auto-delivered by ShipmentTimerBean (eta reached)");
        }

    }
}