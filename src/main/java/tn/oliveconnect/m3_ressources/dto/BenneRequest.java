package tn.oliveconnect.m3_ressources.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record BenneRequest(
        @NotBlank(message = "nom est obligatoire") String nom,
        @NotNull(message = "disponibilite est obligatoire") Boolean disponibilite,
        @NotNull(message = "capacite est obligatoire") @Positive(message = "capacite doit être strictement positive") Double capacite,
        @NotBlank(message = "immatriculation est obligatoire") String immatriculation) {
}
