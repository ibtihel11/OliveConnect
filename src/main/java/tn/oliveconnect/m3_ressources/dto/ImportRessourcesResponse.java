package tn.oliveconnect.m3_ressources.dto;

import java.util.List;

public record ImportRessourcesResponse(
        int totalLignes,
        int nombreImportes,
        int nombreRejetes,
        List<ImportErreurResponse> erreurs) {
}
