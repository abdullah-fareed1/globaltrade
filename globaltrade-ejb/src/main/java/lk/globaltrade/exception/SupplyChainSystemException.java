package lk.globaltrade.exception;

// A SYSTEM exception, deliberately NOT annotated with @ApplicationException.
// Per CONTRACTS.md §10, this means the container wraps it in EJBException
// before it reaches the servlet — that's the intended, documented behavior,
// not a bug. Unchecked because system exceptions are RuntimeExceptions.

public class SupplyChainSystemException extends RuntimeException {
    public SupplyChainSystemException(String message, Throwable cause) {
        super(message, cause);
    }
}