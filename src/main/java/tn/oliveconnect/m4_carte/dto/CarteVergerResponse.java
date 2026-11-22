package tn.oliveconnect.m4_carte.dto;

import java.time.LocalDateTime;
import java.util.List;

/** Carte complète (profils connectés) : en plus de la version publique, statuts, relevés et météo récente. */
public record CarteVergerResponse(String id, String nom, String adresse, Double superficie,
                                  Double latitude, Double longitude,
                                  String statutVerger, String statutRecolte,
                                  String typeOlives, Double niveauMaturite, LocalDateTime dateDernierReleve,
                                  List<ReleveRecentResponse> derniersReleves,
                                  MeteoRecenteResponse meteo) {
}
