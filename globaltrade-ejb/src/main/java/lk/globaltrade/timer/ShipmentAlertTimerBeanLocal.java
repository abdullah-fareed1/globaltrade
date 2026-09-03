package lk.globaltrade.timer;

import jakarta.ejb.Local;

@Local
public interface ShipmentAlertTimerBeanLocal {
    void scheduleReadinessCheck(int shipmentId, long delayMinutes);
}