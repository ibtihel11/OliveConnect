package tn.oliveconnect.m4_carte.dto;

/** Carte publique (Visiteur) : position, maturité, type d'olives. Rien d'autre n'est exposé. */
public record CartePubliqueVergerResponse(String id, String nom, Double latitude, Double longitude,
                                          Double niveauMaturite, String typeOlives) {
}
