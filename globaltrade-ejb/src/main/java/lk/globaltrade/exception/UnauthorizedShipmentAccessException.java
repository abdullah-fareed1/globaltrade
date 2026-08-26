package lk.globaltrade.exception;

import jakarta.ejb.ApplicationException;

// Thrown by getShipmentById() when the caller isn't the shipment's owner.
// The IDOR / resource-level authorization demonstration.
@ApplicationException(rollback = true)
public class UnauthorizedShipmentAccessException extends Exception {
    public UnauthorizedShipmentAccessException(String message) {
        super(message);
    }
}