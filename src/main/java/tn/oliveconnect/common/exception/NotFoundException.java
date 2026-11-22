package tn.oliveconnect.common.exception;

/** Ressource inexistante -> HTTP 404. */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
