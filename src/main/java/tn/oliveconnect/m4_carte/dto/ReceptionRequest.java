package tn.oliveconnect.m4_carte.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import tn.oliveconnect.m4_carte.entity.QualiteReception;

/** Corps de POST /api/receptions (exemple du cadrage 6.5). */
public record ReceptionRequest(
        @NotBlank(message = "tourneeId est obligatoire") String tourneeId,
        @NotBlank(message = "benneId est obligatoire") String benneId,
        @NotBlank(message = "pressoirId est obligatoire") String pressoirId,
        @NotNull(message = "poids est obligatoire")
        @Positive(message = "poids doit être strictement positif") Double poids,
        @NotNull(message = "qualite est obligatoire") QualiteReception qualite) {
}
