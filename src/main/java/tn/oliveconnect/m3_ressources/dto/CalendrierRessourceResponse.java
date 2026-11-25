package tn.oliveconnect.m3_ressources.dto;

import java.util.List;

/** Vue calendrier d'une ressource, construite sans exposer l'entité MongoDB. */
public record CalendrierRessourceResponse(
        String id,
        String nom,
        String typeRessource,
        Boolean disponibilite,
        PeriodeCalendrierResponse periodeDemandee,
        List<CalendrierAffectationResponse> affectations) {
}
