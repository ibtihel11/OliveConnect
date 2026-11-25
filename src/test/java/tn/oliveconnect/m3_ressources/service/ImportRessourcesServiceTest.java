package tn.oliveconnect.m3_ressources.service;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.mock.web.MockMultipartFile;
import tools.jackson.databind.ObjectMapper;
import tn.oliveconnect.common.exception.BadRequestException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImportRessourcesServiceTest {

    private static final ValidatorFactory VALIDATOR_FACTORY = Validation.buildDefaultValidatorFactory();
    private static final Validator VALIDATOR = VALIDATOR_FACTORY.getValidator();

    @Mock OuvrierService ouvrierService;
    @Mock TracteurService tracteurService;
    @Mock BenneService benneService;
    @Mock MaterielService materielService;

    private ImportRessourcesService service;

    @BeforeEach
    void setUp() {
        service = new ImportRessourcesService(new ObjectMapper(), VALIDATOR,
                ouvrierService, tracteurService, benneService, materielService);
    }

    @AfterAll
    static void closeValidatorFactory() {
        VALIDATOR_FACTORY.close();
    }

    @Test
    void importeUnTableauJsonAvecPlusieursTypesM3() {
        MockMultipartFile fichier = fichier("ressources.json", "application/json", """
                [
                  {"ressourceType":"ouvrier","nom":"Amina","disponibilite":true,"specialite":"Taille","saisonnier":false},
                  {"ressourceType":"materiel","nom":"Filets","disponibilite":true,"type":"filet","quantiteStock":12,"seuilMinimum":3}
                ]
                """);

        var rapport = service.importer(fichier);

        assertEquals(2, rapport.totalLignes());
        assertEquals(2, rapport.nombreImportes());
        assertEquals(0, rapport.nombreRejetes());
        verify(ouvrierService).creer(any());
        verify(materielService).creer(any());
    }

    @Test
    void importeUnFichierCsv() {
        MockMultipartFile fichier = fichier("ressources.csv", "text/csv", """
                ressourceType,nom,disponibilite,specialite,saisonnier,immatriculation,puissance,capacite,type,quantiteStock,seuilMinimum
                ouvrier,Amina,true,Taille,false,,,,,,
                tracteur,Tracteur A,true,,,123TU456,90.5,,,,
                benne,Benne A,true,,,789TU456,,1500,,,
                materiel,Filets,true,,,,,,filet,12,3
                """);

        var rapport = service.importer(fichier);

        assertEquals(4, rapport.totalLignes());
        assertEquals(4, rapport.nombreImportes());
        assertEquals(0, rapport.nombreRejetes());
        verify(ouvrierService).creer(any());
        verify(tracteurService).creer(any());
        verify(benneService).creer(any());
        verify(materielService).creer(any());
    }

    @Test
    void rejetteUneLigneInvalideEtRetourneUnRapportDetaille() {
        MockMultipartFile fichier = fichier("ouvriers.csv", "text/csv", """
                nom,disponibilite,specialite,saisonnier
                Amina,true,Taille,false
                ,true,Récolte,true
                """);

        var rapport = service.importer(fichier);

        assertEquals(2, rapport.totalLignes());
        assertEquals(1, rapport.nombreImportes());
        assertEquals(1, rapport.nombreRejetes());
        assertEquals(3, rapport.erreurs().get(0).ligne());
        assertEquals("nom est obligatoire", rapport.erreurs().get(0).motif());
    }

    @Test
    void rejetteLesNombresNegatifsEtLesConversionsImpossible() {
        MockMultipartFile fichier = fichier("bennes.json", "application/json", """
                [
                  {"ressourceType":"benne","nom":"Benne A","disponibilite":true,"immatriculation":"123TU456","capacite":-1},
                  {"ressourceType":"materiel","nom":"Filet","disponibilite":true,"type":"filet","quantiteStock":"abc","seuilMinimum":1}
                ]
                """);
        var rapport = service.importer(fichier);
        assertEquals(2, rapport.nombreRejetes());
        assertEquals(0, rapport.nombreImportes());
        assertEquals(2, rapport.erreurs().size());
    }

    @Test
    void rejetteUnFichierVideEtUnFormatNonPrisEnCharge() {
        assertThrows(BadRequestException.class,
                () -> service.importer(fichier("vide.json", "application/json", "")));
        assertThrows(BadRequestException.class,
                () -> service.importer(fichier("ressources.xml", "application/xml", "<resources/>")));
    }

    @Test
    void rapporteLesDoublonsDeclaresParUneContrainteMongoSansEcraser() {
        doThrow(new DuplicateKeyException("duplicate key"))
                .when(benneService).creer(any());
        MockMultipartFile fichier = fichier("bennes.json", "application/json", """
                [{"ressourceType":"benne","nom":"Benne A","disponibilite":true,"immatriculation":"123TU456","capacite":1000}]
                """);

        var rapport = service.importer(fichier);

        assertEquals(0, rapport.nombreImportes());
        assertEquals(1, rapport.nombreRejetes());
        assertEquals("Doublon rejeté par une contrainte d'unicité de la base de données",
                rapport.erreurs().get(0).motif());
    }

    @Test
    void importeUnTracteurAvecNouvelleImmatriculationEtNettoieLesEspacesExterieurs() {
        when(tracteurService.existeImmatriculation("123 TU 456")).thenReturn(false);
        MockMultipartFile fichier = fichier("tracteurs.json", "application/json", """
                [{"ressourceType":"tracteur","nom":"T1","disponibilite":true,"immatriculation":" 123 TU 456 ","puissance":90}]
                """);

        var rapport = service.importer(fichier);

        assertEquals(1, rapport.totalLignes());
        assertEquals(1, rapport.nombreImportes());
        assertEquals(0, rapport.nombreRejetes());
        verify(tracteurService).creer(org.mockito.ArgumentMatchers.argThat(request ->
                request.immatriculation().equals("123 TU 456")));
        verify(tracteurService).existeImmatriculation("123 TU 456");
    }

    @Test
    void rejetteUnTracteurDejaEnBaseSansLeModifier() {
        when(tracteurService.existeImmatriculation("123 tu 456")).thenReturn(true);
        MockMultipartFile fichier = fichier("tracteurs.json", "application/json", """
                [{"ressourceType":"tracteur","nom":"T2","disponibilite":true,"immatriculation":" 123 tu 456 ","puissance":95}]
                """);

        var rapport = service.importer(fichier);

        assertEquals(0, rapport.nombreImportes());
        assertEquals(1, rapport.nombreRejetes());
        assertEquals(1, rapport.erreurs().get(0).ligne());
        assertEquals("Immatriculation déjà présente dans la collection tracteurs",
                rapport.erreurs().get(0).motif());
        verify(tracteurService, never()).creer(any());
    }

    @Test
    void rejetteUnDoublonDeTracteurDansLeMemeFichierEtContinueLesLignesSuivantes() {
        when(tracteurService.existeImmatriculation(anyString())).thenReturn(false);
        MockMultipartFile fichier = fichier("tracteurs.json", "application/json", """
                [
                  {"ressourceType":"tracteur","nom":"T1","disponibilite":true,"immatriculation":" 123 TU 456 ","puissance":90},
                  {"ressourceType":"tracteur","nom":"T2","disponibilite":true,"immatriculation":"123 tu 456","puissance":95},
                  {"ressourceType":"tracteur","nom":"T3","disponibilite":true,"immatriculation":" 999 TU 888 ","puissance":80}
                ]
                """);

        var rapport = service.importer(fichier);

        assertEquals(3, rapport.totalLignes());
        assertEquals(2, rapport.nombreImportes());
        assertEquals(1, rapport.nombreRejetes());
        assertEquals(2, rapport.erreurs().get(0).ligne());
        org.junit.jupiter.api.Assertions.assertTrue(rapport.erreurs().get(0).motif().contains("répétée"));
        verify(tracteurService, times(2)).creer(any());
        verify(tracteurService, times(2)).existeImmatriculation(anyString());
    }

    @Test
    void benneRejetteLeDoublonCsvEtLaVerificationResteIndependanteDesTracteurs() {
        when(benneService.existeImmatriculation(anyString())).thenReturn(false);
        when(tracteurService.existeImmatriculation("123 TU 456")).thenReturn(false);
        MockMultipartFile fichier = fichier("ressources.csv", "text/csv", """
                ressourceType,nom,disponibilite,immatriculation,capacite,puissance
                benne,B1,true, 123 TU 456 ,1200,
                tracteur,T1,true,123 TU 456,,90
                benne,B2,true,123 tu 456,1500,
                benne,B3,true,999 TU 888,1000,
                """);

        var rapport = service.importer(fichier);

        assertEquals(4, rapport.totalLignes());
        assertEquals(3, rapport.nombreImportes());
        assertEquals(1, rapport.nombreRejetes());
        assertEquals(4, rapport.erreurs().get(0).ligne());
        org.junit.jupiter.api.Assertions.assertTrue(rapport.erreurs().get(0).motif().contains("répétée"));
        verify(benneService, times(2)).creer(any());
        verify(tracteurService).creer(any());
        verify(tracteurService, never()).existeImmatriculation("123 tu 456");
    }

    @Test
    void rejetteUneImmatriculationDeBenneDejaEnBaseAvecMotifExplicite() {
        when(benneService.existeImmatriculation("ABC 123")).thenReturn(true);
        MockMultipartFile fichier = fichier("bennes.json", "application/json", """
                [{"ressourceType":"benne","nom":"B1","disponibilite":true,"immatriculation":" ABC 123 ","capacite":1000}]
                """);

        var rapport = service.importer(fichier);

        assertEquals(1, rapport.nombreRejetes());
        assertEquals(1, rapport.erreurs().get(0).ligne());
        assertEquals("Immatriculation déjà présente dans la collection bennes",
                rapport.erreurs().get(0).motif());
        verify(benneService, never()).creer(any());
    }

    @Test
    void accepteLesMemesAttributsPourOuvriersEtMateriels() {
        MockMultipartFile fichier = fichier("ressources.json", "application/json", """
                [
                  {"ressourceType":"ouvrier","nom":"Amina","disponibilite":true,"specialite":"Taille","saisonnier":false},
                  {"ressourceType":"ouvrier","nom":"Amina","disponibilite":true,"specialite":"Taille","saisonnier":false},
                  {"ressourceType":"materiel","nom":"Filets","disponibilite":true,"type":"filet","quantiteStock":12,"seuilMinimum":3},
                  {"ressourceType":"materiel","nom":"Filets","disponibilite":true,"type":"filet","quantiteStock":12,"seuilMinimum":3}
                ]
                """);

        var rapport = service.importer(fichier);

        assertEquals(4, rapport.nombreImportes());
        assertEquals(0, rapport.nombreRejetes());
        verify(ouvrierService, times(2)).creer(any());
        verify(materielService, times(2)).creer(any());
    }

    private MockMultipartFile fichier(String nom, String typeMime, String contenu) {
        return new MockMultipartFile("file", nom, typeMime, contenu.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }
}
