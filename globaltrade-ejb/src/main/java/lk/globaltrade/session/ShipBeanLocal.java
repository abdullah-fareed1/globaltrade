// Path: globaltrade-ejb/src/main/java/lk/globaltrade/session/ShipBeanLocal.java
package lk.globaltrade.session;

import jakarta.ejb.Local;
import lk.globaltrade.entities.Ship;

import java.util.List;

/**
 * Local business interface for {@link ShipBean}.
 *
 * Signature is frozen by CONTRACTS.md §3 — do not add, remove, or
 * reorder parameters.
 */
@Local
public interface ShipBeanLocal {

    List<Ship> findAll();

    /**
     * Creates a new ship in status AT_PORT, located at the given port.
     *
     * @param name         ship name
     * @param capacity     capacity
     * @param initialPortId id of the Port the ship starts at
     */
    Ship create(String name, int capacity, int initialPortId);

    /**
     * Updates only {@code status}. Deliberately does NOT touch
     * currentPort — per ENTITIES.md, currentPort is written exclusively
     * by ShipmentTimerBean (Phase 4).
     */
    void updateStatus(int shipId, Ship.Status newStatus);
}