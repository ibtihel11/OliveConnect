package tn.oliveconnect.m3_ressources.service;

import tn.oliveconnect.common.exception.BadRequestException;

import java.time.LocalDateTime;

/** Validation commune d'une période de recherche de disponibilité. */
final class PeriodeDisponibilite {

    private PeriodeDisponibilite() {
    }

    static void valider(LocalDateTime debut, LocalDateTime fin) {
        if (debut == null || fin == null || !debut.isBefore(fin)) {
            throw new BadRequestException("La date de début doit être antérieure à la date de fin");
        }
    }
}
