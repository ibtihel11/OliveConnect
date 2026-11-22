package tn.oliveconnect.m4_carte.dto;

public record RemplissageBenneResponse(String benneId, String immatriculation, Double capaciteKg,
                                       long nombreReceptions, Double poidsMoyenKg,
                                       Double tauxRemplissageMoyenPourcent, Double tauxRemplissageMaxPourcent) {
}
