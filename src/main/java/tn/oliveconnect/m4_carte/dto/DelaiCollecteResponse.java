package tn.oliveconnect.m4_carte.dto;

import java.util.List;

/**
 * Délais de collecte : nombre de jours entre la date souhaitée de la récolte et le début de sa tournée
 * (tournées EN_COURS ou TERMINEE, récoltes non annulées).
 */
public record DelaiCollecteResponse(long nombreRecoltes, Double delaiMoyenJours, Double delaiMinJours,
                                    Double delaiMaxJours, List<DelaiParTypeResponse> parType,
                                    List<DelaiParVergerResponse> parVerger) {
}
