package tn.oliveconnect.m3_ressources.dto;

import tn.oliveconnect.m3_ressources.entity.Benne;

public record BenneResponse(String id, String nom, Boolean disponibilite, Double capacite, String immatriculation) {

    public static BenneResponse from(Benne benne) {
        return new BenneResponse(benne.getId(), benne.getNom(), benne.getDisponibilite(),
                benne.getCapacite(), benne.getImmatriculation());
    }
}
