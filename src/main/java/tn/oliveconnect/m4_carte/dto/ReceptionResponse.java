package tn.oliveconnect.m4_carte.dto;

import tn.oliveconnect.m4_carte.entity.QualiteReception;
import tn.oliveconnect.m4_carte.entity.Reception;

import java.time.LocalDateTime;

public record ReceptionResponse(String id, String tourneeId, String benneId, String pressoirId,
                                LocalDateTime dateReception, Double poids, QualiteReception qualite) {

    public static ReceptionResponse from(Reception r) {
        return new ReceptionResponse(r.getId(), r.getTourneeId(), r.getBenneId(), r.getPressoirId(),
                r.getDateReception(), r.getPoids(), r.getQualite());
    }
}
