package tn.oliveconnect.m4_carte.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PressoirRequest(
        @NotBlank(message = "nom est obligatoire") String nom,
        @NotBlank(message = "adresse est obligatoire") String adresse,
        @NotNull(message = "capaciteTraitement est obligatoire")
        @Positive(message = "capaciteTraitement doit être strictement positive") Double capaciteTraitement) {
}
