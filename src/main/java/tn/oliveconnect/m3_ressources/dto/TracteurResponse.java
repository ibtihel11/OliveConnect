package tn.oliveconnect.m3_ressources.dto;

import tn.oliveconnect.m3_ressources.entity.Tracteur;

public record TracteurResponse(String id, String nom, Boolean disponibilite, String immatriculation, Double puissance) {

    public static TracteurResponse from(Tracteur tracteur) {
        return new TracteurResponse(tracteur.getId(), tracteur.getNom(), tracteur.getDisponibilite(),
                tracteur.getImmatriculation(), tracteur.getPuissance());
    }
}
