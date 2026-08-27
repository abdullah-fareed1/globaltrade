package lk.globaltrade.timer;

import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.ejb.Lock;
import jakarta.ejb.LockType;
import jakarta.ejb.Singleton;
import jakarta.ejb.Timeout;
import jakarta.ejb.Timer;
import jakarta.ejb.TimerConfig;
import jakarta.ejb.TimerService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import lk.globaltrade.entities.Shipment;
import lk.globaltrade.session.AuditLogWriterBeanLocal;

/**
 * Programmatic timer: ShipmentBookingBean calls scheduleReadinessCheck()
 * right after booking. 30 minutes later, handleTimeout() checks whether the
 * shipment moved off PENDING and writes the appropriate audit row.
 *
 * persistent = true so the timer survives a Payara restart. That also means
 * a stale timer from a previous deploy can fire against newer code — if
 * unexplained SHIPMENT_ALERT rows show up during testing, clear the domain's
 * timer store. Worth a sentence in the reliability write-up.
 */
@Singleton
public class ShipmentAlertTimerBean implements ShipmentAlertTimerBeanLocal {

    @PersistenceContext(unitName = "globaltradePU")
    private EntityManager em;

    @Resource
    private TimerService timerService;

    @EJB
    private AuditLogWriterBeanLocal auditLogWriter;

    /**
     * Default singleton locking is WRITE, which would serialise every
     * booking in the application behind this one method. Creating a timer
     * is thread-safe on its own, so READ costs nothing and buys back
     * throughput.
     */
    @Override
    @Lock(LockType.READ)
    public void scheduleReadinessCheck(int shipmentId, long delayMinutes) {
        TimerConfig config = new TimerConfig(shipmentId, true); // persistent=true
        timerService.createSingleActionTimer(delayMinutes * 60_000L, config);
    }

    @Timeout
    public void handleTimeout(Timer timer) {
        int shipmentId = (Integer) timer.getInfo();

        Shipment shipment = em.find(Shipment.class, shipmentId);
        if (shipment == null) {
            // Shipment no longer exists (shouldn't happen in practice, but
            // a stale persistent timer from an earlier deploy could point
            // at a row that's gone). Nothing to alert on.
            return;
        }

        if (shipment.getStatus() == Shipment.Status.PENDING) {
            auditLogWriter.writeLog(null, "SHIPMENT_ALERT", "Shipment",
                    shipment.getId(),
                    "Shipment " + shipment.getId()
                            + " still PENDING 30 minutes after booking");
        } else {
            auditLogWriter.writeLog(null, "READINESS_CONFIRMED", "Shipment",
                    shipment.getId(),
                    "Shipment " + shipment.getId()
                            + " progressed to " + shipment.getStatus()
                            + " before the readiness check fired");
        }
    }
}