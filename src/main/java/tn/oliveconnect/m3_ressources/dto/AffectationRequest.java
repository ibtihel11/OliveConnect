package tn.oliveconnect.m3_ressources.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import tn.oliveconnect.m3_ressources.entity.TypeRessource;

import java.time.LocalDateTime;

public record AffectationRequest(
        @NotBlank(message = "ressourceId est obligatoire") String ressourceId,
        @NotNull(message = "ressourceType est obligatoire") TypeRessource ressourceType,
        @NotBlank(message = "tourneeId est obligatoire") String tourneeId,
        @NotNull(message = "dateDebut est obligatoire") LocalDateTime dateDebut,
        @NotNull(message = "dateFin est obligatoire") LocalDateTime dateFin,
        @NotBlank(message = "statut est obligatoire") String statut) {
}
