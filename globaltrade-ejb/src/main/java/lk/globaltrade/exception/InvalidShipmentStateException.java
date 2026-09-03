package lk.globaltrade.exception;

import jakarta.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class InvalidShipmentStateException extends Exception {
    public InvalidShipmentStateException(String message) {
        super(message);
    }
}