package tn.oliveconnect.m3_ressources.dto;

import tn.oliveconnect.m3_ressources.entity.Ouvrier;

public record OuvrierResponse(String id, String nom, Boolean disponibilite, String specialite, Boolean saisonnier) {

    public static OuvrierResponse from(Ouvrier ouvrier) {
        return new OuvrierResponse(ouvrier.getId(), ouvrier.getNom(), ouvrier.getDisponibilite(),
                ouvrier.getSpecialite(), ouvrier.getSaisonnier());
    }
}
