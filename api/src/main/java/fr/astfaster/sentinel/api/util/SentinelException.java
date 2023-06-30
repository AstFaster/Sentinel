package fr.astfaster.sentinel.api.util;

public class SentinelException extends RuntimeException {

    public SentinelException(String message) {
        super(message);
    }

    public SentinelException(String message, Throwable cause) {
        super(message, cause);
    }

    public SentinelException(Throwable cause) {
        super(cause);
    }

}
