package lk.globaltrade.exception;

import jakarta.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class NoContainerAvailableException extends Exception {
    public NoContainerAvailableException(String message) {
        super(message);
    }
}