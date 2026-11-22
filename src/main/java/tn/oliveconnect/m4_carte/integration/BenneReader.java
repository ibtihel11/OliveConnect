package tn.oliveconnect.m4_carte.integration;

import java.util.Optional;

/** Port d'accès à M3 (Benne). Implémentation actuelle : lecture seule de la collection "bennes". */
public interface BenneReader {
    Optional<BenneRef> findById(String id);
}
