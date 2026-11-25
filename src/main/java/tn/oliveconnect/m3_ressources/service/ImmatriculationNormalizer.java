package tn.oliveconnect.m3_ressources.service;

import java.util.Locale;
import java.util.regex.Pattern;

/** Normalisation utilisée uniquement pour comparer les immatriculations à l'import. */
public final class ImmatriculationNormalizer {

    private ImmatriculationNormalizer() {
    }

    /** Valeur nettoyée à conserver : les espaces internes et la casse sont préservés. */
    public static String nettoyer(String immatriculation) {
        return immatriculation == null ? null : immatriculation.trim();
    }

    /** Clé de comparaison insensible à la casse; les espaces internes restent significatifs. */
    public static String cle(String immatriculation) {
        String nettoyee = nettoyer(immatriculation);
        return nettoyee == null ? null : nettoyee.toLowerCase(Locale.ROOT);
    }

    /** Regex Mongo qui tolère les espaces extérieurs déjà présents en base sans toucher aux espaces internes. */
    public static String regexEquivalent(String immatriculation) {
        return "^ *" + Pattern.quote(nettoyer(immatriculation)) + " *$";
    }
}
