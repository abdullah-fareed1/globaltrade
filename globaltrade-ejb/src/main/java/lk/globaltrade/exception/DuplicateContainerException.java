package lk.globaltrade.exception;

import jakarta.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class DuplicateContainerException extends Exception {
    public DuplicateContainerException(String message) {
        super(message);
    }
}
