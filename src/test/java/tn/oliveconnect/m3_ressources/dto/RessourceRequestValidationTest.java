package tn.oliveconnect.m3_ressources.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RessourceRequestValidationTest {

    private static Validator validator;
    private static jakarta.validation.ValidatorFactory factory;

    @BeforeAll
    static void initialiserValidateur() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void fermerValidateur() {
        factory.close();
    }

    @Test
    void valideLesDonneesObligatoiresDeChaqueRessource() {
        assertFalse(validator.validate(new OuvrierRequest(" ", null, "", null)).isEmpty());
        assertFalse(validator.validate(new TracteurRequest("Tracteur", true, " ", -1.0)).isEmpty());
        assertFalse(validator.validate(new BenneRequest("Benne", true, 0.0, "123TU456")).isEmpty());
        assertFalse(validator.validate(new MaterielRequest("Filets", true, "FILETS", -1, 0)).isEmpty());
    }

    @Test
    void accepteLesDonneesValidesEtLeStockNul() {
        assertTrue(validator.validate(new OuvrierRequest("Olivier", true, "Récolte", false)).isEmpty());
        assertTrue(validator.validate(new TracteurRequest("Tracteur", true, "123TU456", 90.0)).isEmpty());
        assertTrue(validator.validate(new BenneRequest("Benne", true, 1000.0, "234TU567")).isEmpty());
        assertTrue(validator.validate(new MaterielRequest("Filets", true, "FILETS", 0, 0)).isEmpty());
    }
}
