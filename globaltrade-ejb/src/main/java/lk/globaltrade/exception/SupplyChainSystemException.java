package lk.globaltrade.exception;

public class SupplyChainSystemException extends RuntimeException {

    public SupplyChainSystemException(String message) {
        super(message);
    }

    public SupplyChainSystemException(String message, Throwable cause) {
        super(message, cause);
    }
}