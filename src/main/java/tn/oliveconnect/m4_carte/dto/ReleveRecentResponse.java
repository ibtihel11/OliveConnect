package tn.oliveconnect.m4_carte.dto;

import java.time.LocalDateTime;

/** Relevé de maturité imbriqué dans vergers.derniersReleves ({date, niveauMaturite}). */
public record ReleveRecentResponse(LocalDateTime date, Double niveauMaturite) {
}
