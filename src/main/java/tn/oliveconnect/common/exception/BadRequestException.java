package tn.oliveconnect.common.exception;

/** Données invalides -> HTTP 400. */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
