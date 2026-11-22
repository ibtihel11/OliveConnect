package tn.oliveconnect.common.exception;

/** Droits insuffisants (règle métier de propriété) -> HTTP 403. */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
