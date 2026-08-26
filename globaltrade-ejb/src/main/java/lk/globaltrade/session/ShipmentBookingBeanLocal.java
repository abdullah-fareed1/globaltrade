// Path: globaltrade-ejb/src/main/java/lk/globaltrade/session/ShipmentBookingBeanLocal.java
package lk.globaltrade.session;

import jakarta.ejb.Local;
import lk.globaltrade.entities.Shipment;
import lk.globaltrade.exception.NoContainerAvailableException;

/**
 * Local business interface for {@link ShipmentBookingBean}.
 *
 * Signature is frozen by CONTRACTS.md §3 — do not add, remove, or
 * reorder parameters.
 */
@Local
public interface ShipmentBookingBeanLocal {

    /**
     * Reserves {@code containerCount} AVAILABLE containers and creates a
     * new PENDING Shipment. See CONTRACTS.md §12 for the full rule set.
     *
     * @throws NoContainerAvailableException if fewer than
     *         {@code containerCount} containers are AVAILABLE
     *         (application exception, rollback=true — nothing is
     *         reserved or persisted)
     */
    Shipment bookShipment(int customerId, int originPortId, int destinationPortId, int containerCount)
            throws NoContainerAvailableException;
}