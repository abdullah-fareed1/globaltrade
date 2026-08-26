// Path: globaltrade-ejb/src/main/java/lk/globaltrade/session/ShipmentOperationsBeanLocal.java
package lk.globaltrade.session;

import jakarta.ejb.Local;
import lk.globaltrade.entities.Shipment;
import lk.globaltrade.exception.InvalidShipmentStateException;
import lk.globaltrade.exception.UnauthorizedShipmentAccessException;

import java.util.List;

/**
 * Local business interface for {@link ShipmentOperationsBean}.
 *
 * Signature is frozen by CONTRACTS.md §3 — do not add, remove, or
 * reorder parameters.
 */
@Local
public interface ShipmentOperationsBeanLocal {

    List<Shipment> viewActiveShipments();

    /**
     * @throws InvalidShipmentStateException if the requested transition
     *         is not legal from the shipment's current status
     *         (CONTRACTS.md §12), or the shipment does not exist
     */
    void updateStatus(int shipmentId, Shipment.Status newStatus) throws InvalidShipmentStateException;

    /**
     * No parameter, by design (CONTRACTS.md §5 / BUILD_PLAN FIX 7):
     * identity comes from the container's caller principal, never from
     * a client-supplied id, which removes the tampering vector the
     * original {@code getOwnShipments(int)} signature had.
     */
    List<Shipment> getOwnShipments();

    /**
     * Resource-level authorization / IDOR guard: a caller may only read
     * a shipment they own.
     *
     * @throws UnauthorizedShipmentAccessException if the shipment does
     *         not exist, or the caller is not its customer
     */
    Shipment getShipmentById(int shipmentId) throws UnauthorizedShipmentAccessException;
}