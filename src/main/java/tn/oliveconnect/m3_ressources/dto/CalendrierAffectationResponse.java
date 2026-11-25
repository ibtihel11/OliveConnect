package tn.oliveconnect.m3_ressources.dto;

import java.time.LocalDateTime;

/** Affectation réelle d'une ressource; n'expose pas tourneeId, qui reste une référence M1 non vérifiée. */
public record CalendrierAffectationResponse(
        String id,
        LocalDateTime dateDebut,
        LocalDateTime dateFin,
        String statut) {
}
