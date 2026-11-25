package tn.oliveconnect.m3_ressources.dto;

import tn.oliveconnect.m3_ressources.entity.Materiel;

public record MaterielResponse(String id, String nom, Boolean disponibilite, String type,
                               Integer quantiteStock, Integer seuilMinimum) {

    public static MaterielResponse from(Materiel materiel) {
        return new MaterielResponse(materiel.getId(), materiel.getNom(), materiel.getDisponibilite(),
                materiel.getType(), materiel.getQuantiteStock(), materiel.getSeuilMinimum());
    }
}
