package tn.oliveconnect.m3_ressources.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OuvrierRequest(
        @NotBlank(message = "nom est obligatoire") String nom,
        @NotNull(message = "disponibilite est obligatoire") Boolean disponibilite,
        @NotBlank(message = "specialite est obligatoire") String specialite,
        @NotNull(message = "saisonnier est obligatoire") Boolean saisonnier) {
}
