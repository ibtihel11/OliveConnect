package tn.oliveconnect.m3_ressources.dto;

import java.time.LocalDateTime;

/** Période demandée par le client, sans implication de réservation. */
public record PeriodeCalendrierResponse(LocalDateTime debut, LocalDateTime fin) {
}
