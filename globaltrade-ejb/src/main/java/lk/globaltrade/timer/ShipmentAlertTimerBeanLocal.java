package lk.globaltrade.timer;

import jakarta.ejb.Local;

/**
 * Programmatic timer seam. ShipmentBookingBean calls this at the end of
 * bookShipment() to schedule a 30-minute readiness check (see Phase 2 TODO
 * and CONTRACTS.md §12, step 6).
 */
@Local
public interface ShipmentAlertTimerBeanLocal {

    /**
     * Schedules a single-action, persistent timer that fires in
     * {@code delayMinutes} minutes and checks whether the shipment has
     * moved off PENDING yet.
     *
     * @param shipmentId   the shipment to check
     * @param delayMinutes minutes from now until the check fires
     */
    void scheduleReadinessCheck(int shipmentId, long delayMinutes);
}