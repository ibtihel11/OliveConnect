package tn.oliveconnect.m4_carte.integration;

import java.util.Optional;

/**
 * Port d'accès à M1 (TourneeService). Implémentation actuelle : lecture seule de la collection "tournees".
 * Quand M1 livre TourneeService, remplacer l'implémentation sans toucher au reste de M4.
 */
public interface TourneeReader {
    Optional<TourneeRef> findById(String id);
}
