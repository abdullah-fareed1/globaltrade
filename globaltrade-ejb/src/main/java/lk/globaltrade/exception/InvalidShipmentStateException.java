package lk.globaltrade.exception;

import jakarta.ejb.ApplicationException;

// Thrown when updateStatus() is given an illegal status transition.
@ApplicationException(rollback = true)
public class InvalidShipmentStateException extends Exception {
    public InvalidShipmentStateException(String message) {
        super(message);
    }
}