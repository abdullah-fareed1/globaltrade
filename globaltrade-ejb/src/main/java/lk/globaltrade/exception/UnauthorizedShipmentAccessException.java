package lk.globaltrade.exception;

import jakarta.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class UnauthorizedShipmentAccessException extends Exception {
    public UnauthorizedShipmentAccessException(String message) {
        super(message);
    }
}