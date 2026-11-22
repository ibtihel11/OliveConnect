package tn.oliveconnect.m4_carte.dto;

/**
 * Rendement d'un verger.
 * superficie en hectares ; poids en kg ; rendementKgParHa = poidsRecuPressoirKg / superficie ;
 * tauxReceptionPourcent = poidsRecuPressoirKg / quantiteRecolteeKg x 100.
 */
public record RendementVergerResponse(String vergerId, String nomVerger, Double superficie,
                                      Double quantiteRecolteeKg, Double poidsRecuPressoirKg,
                                      Double rendementKgParHa, Double tauxReceptionPourcent,
                                      long nombreTournees) {
}
