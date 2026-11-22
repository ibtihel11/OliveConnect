package tn.oliveconnect.common.exception;

import java.time.LocalDateTime;

/** Format d'erreur unique (cadrage 6.1). */
public record ApiError(LocalDateTime timestamp, int status, String error, String message, String path) {
}
