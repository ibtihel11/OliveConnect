package tn.oliveconnect.m3_ressources.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record MaterielRequest(
        @NotBlank(message = "nom est obligatoire") String nom,
        @NotNull(message = "disponibilite est obligatoire") Boolean disponibilite,
        @NotBlank(message = "type est obligatoire") String type,
        @NotNull(message = "quantiteStock est obligatoire") @PositiveOrZero(message = "quantiteStock ne peut pas être négative") Integer quantiteStock,
        @NotNull(message = "seuilMinimum est obligatoire") @PositiveOrZero(message = "seuilMinimum ne peut pas être négatif") Integer seuilMinimum) {
}
