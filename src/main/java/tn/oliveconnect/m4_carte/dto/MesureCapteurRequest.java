package tn.oliveconnect.m4_carte.dto;

import java.time.LocalDateTime;

/** Mesure JSON fournie par le service météo / capteurs (système externe simulé). */
public record MesureCapteurRequest(String vergerId, LocalDateTime dateReleve,
                                   Double temperature, Double humidite, Double precipitation,
                                   Double niveauMaturite) {
}
