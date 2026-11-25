package tn.oliveconnect.m3_ressources.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class ImmatriculationNormalizerTest {

    @Test
    void nettoieLesExtremitesEtCompareSansCasse() {
        assertEquals("123 TU 456", ImmatriculationNormalizer.nettoyer(" 123 TU 456 "));
        assertEquals(ImmatriculationNormalizer.cle(" 123 TU 456 "),
                ImmatriculationNormalizer.cle("123 tu 456"));
    }

    @Test
    void conserveLesEspacesInternesDansLaComparaisonEtLaRechercheMongo() {
        assertNotEquals(ImmatriculationNormalizer.cle("123 TU 456"),
                ImmatriculationNormalizer.cle("123TU456"));
        assertEquals("^ *\\Q123 TU 456\\E *$",
                ImmatriculationNormalizer.regexEquivalent(" 123 TU 456 "));
    }
}
