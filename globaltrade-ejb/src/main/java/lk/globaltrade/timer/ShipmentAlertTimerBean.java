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

@Singleton
public class ShipmentAlertTimerBean implements ShipmentAlertTimerBeanLocal {

    @PersistenceContext(unitName = "globaltradePU")
    private EntityManager em;

    @Resource
    private TimerService timerService;

    @EJB
    private AuditLogWriterBeanLocal auditLogWriter;

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