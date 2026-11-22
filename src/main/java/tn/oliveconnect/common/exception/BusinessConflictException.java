package tn.oliveconnect.common.exception;

/** Conflit métier -> HTTP 409. */
public class BusinessConflictException extends RuntimeException {
    public BusinessConflictException(String message) {
        super(message);
    }
}
