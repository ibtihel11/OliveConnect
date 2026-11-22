package tn.oliveconnect.common.security;

/** Identité extraite d'un JWT : sub = id utilisateur, claim "role" = nom du rôle. */
public record AuthenticatedUser(String id, String role) {
}
