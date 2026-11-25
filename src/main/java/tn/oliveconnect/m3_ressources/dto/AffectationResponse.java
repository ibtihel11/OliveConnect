package tn.oliveconnect.m3_ressources.dto;

import tn.oliveconnect.m3_ressources.entity.Affectation;
import tn.oliveconnect.m3_ressources.entity.TypeRessource;

import java.time.LocalDateTime;

public record AffectationResponse(
        String id,
        String ressourceId,
        TypeRessource ressourceType,
        String tourneeId,
        LocalDateTime dateDebut,
        LocalDateTime dateFin,
        String statut) {

    public static AffectationResponse from(Affectation affectation) {
        return new AffectationResponse(affectation.getId(), affectation.getRessourceId(),
                affectation.getRessourceType(), affectation.getTourneeId(), affectation.getDateDebut(),
                affectation.getDateFin(), affectation.getStatut());
    }
}
