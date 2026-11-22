package tn.oliveconnect.m4_carte.dto;

import java.util.List;

/** Taux de remplissage = poids réceptionné / capacité de la benne x 100. */
public record RemplissageBennesResponse(long nombreReceptions, Double tauxRemplissageMoyenPourcent,
                                        List<RemplissageBenneResponse> parBenne) {
}
