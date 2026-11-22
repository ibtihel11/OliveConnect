package tn.oliveconnect.m4_carte.dto;

import tn.oliveconnect.m4_carte.entity.Pressoir;

public record PressoirResponse(String id, String nom, String adresse, Double capaciteTraitement, String responsableId) {

    public static PressoirResponse from(Pressoir p) {
        return new PressoirResponse(p.getId(), p.getNom(), p.getAdresse(), p.getCapaciteTraitement(), p.getResponsableId());
    }
}
