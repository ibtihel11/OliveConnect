package tn.oliveconnect.m3_ressources.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TracteurRequest(
        @NotBlank(message = "nom est obligatoire") String nom,
        @NotNull(message = "disponibilite est obligatoire") Boolean disponibilite,
        @NotBlank(message = "immatriculation est obligatoire") String immatriculation,
        @NotNull(message = "puissance est obligatoire") @Positive(message = "puissance doit être strictement positive") Double puissance) {
}
