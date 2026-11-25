package tn.oliveconnect.m3_ressources.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tn.oliveconnect.common.exception.GlobalExceptionHandler;
import tn.oliveconnect.m3_ressources.controller.AffectationController;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.oliveconnect.common.exception.BusinessConflictException;
import tn.oliveconnect.m3_ressources.dto.AffectationRequest;
import tn.oliveconnect.m3_ressources.entity.Affectation;
import tn.oliveconnect.m3_ressources.entity.Benne;
import tn.oliveconnect.m3_ressources.entity.Materiel;
import tn.oliveconnect.m3_ressources.entity.Ouvrier;
import tn.oliveconnect.m3_ressources.entity.Tracteur;
import tn.oliveconnect.m3_ressources.entity.TypeRessource;
import tn.oliveconnect.m3_ressources.repository.AffectationRepository;
import tn.oliveconnect.m3_ressources.repository.BenneRepository;
import tn.oliveconnect.m3_ressources.repository.MaterielRepository;
import tn.oliveconnect.m3_ressources.repository.OuvrierRepository;
import tn.oliveconnect.m3_ressources.repository.TracteurRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@ExtendWith(MockitoExtension.class)
class AffectationServiceConcurrencyTest {

    @Mock AffectationRepository affectationRepository;
    @Mock OuvrierRepository ouvrierRepository;
    @Mock TracteurRepository tracteurRepository;
    @Mock BenneRepository benneRepository;
    @Mock MaterielRepository materielRepository;

    private final ConcurrentLinkedQueue<Affectation> enregistrees = new ConcurrentLinkedQueue<>();
    private final AtomicInteger prochainId = new AtomicInteger();
    private final AtomicBoolean echouerProchaineSauvegarde = new AtomicBoolean();
    private AffectationService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        enregistrees.clear();
        prochainId.set(0);
        echouerProchaineSauvegarde.set(false);
        service = new AffectationService(affectationRepository, ouvrierRepository, tracteurRepository,
                benneRepository, materielRepository, new VerrouAffectationRessource());
        mockMvc = MockMvcBuilders.standaloneSetup(new AffectationController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        when(affectationRepository.save(any(Affectation.class))).thenAnswer(invocation -> {
            if (echouerProchaineSauvegarde.compareAndSet(true, false)) {
                throw new IllegalStateException("échec simulé");
            }
            Affectation affectation = invocation.getArgument(0);
            affectation.setId("a" + prochainId.incrementAndGet());
            enregistrees.add(affectation);
            return affectation;
        });
    }

    @Test
    void deuxRequetesHttpSimultaneesChevauchantesNeCreentQuUneAffectationEtLaSecondeRenvoie409() throws Exception {
        rendreDisponible(TypeRessource.TRACTEUR, "r1");
        CountDownLatch deuxLectures = new CountDownLatch(2);
        installerVerificationAvecBarrier(deuxLectures);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch prets = new CountDownLatch(2);
        CountDownLatch depart = new CountDownLatch(1);
        try {
            Future<Integer> premier = soumettreHttp(pool, prets, depart, requeteJson("r1", "TRACTEUR", 8, 12));
            Future<Integer> second = soumettreHttp(pool, prets, depart, requeteJson("r1", "TRACTEUR", 9, 13));
            assertTrue(prets.await(2, TimeUnit.SECONDS));
            depart.countDown();

            List<Integer> resultats = List.of(premier.get(5, TimeUnit.SECONDS), second.get(5, TimeUnit.SECONDS));
            assertEquals(1, resultats.stream().filter(status -> status == 201).count());
            assertEquals(1, resultats.stream().filter(status -> status == 409).count());
            assertEquals(1, enregistrees.size());
        } finally {
            depart.countDown();
            pool.shutdownNow();
        }
    }

    @Test
    void deuxRessourcesDifferentesPeuventEntrerEnSectionCritiqueEnParallele() throws Exception {
        rendreDisponible(TypeRessource.TRACTEUR, "r1");
        rendreDisponible(TypeRessource.TRACTEUR, "r2");
        CountDownLatch premiereLectureEntree = new CountDownLatch(1);
        CountDownLatch libererPremiereLecture = new CountDownLatch(1);
        installerVerificationBloquantRessource("r1", premiereLectureEntree, libererPremiereLecture);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Future<Resultat> premier = pool.submit(() -> executer(demande("r1", TypeRessource.TRACTEUR, 8, 12)));
            assertTrue(premiereLectureEntree.await(2, TimeUnit.SECONDS));
            Future<Resultat> second = pool.submit(() -> executer(demande("r2", TypeRessource.TRACTEUR, 8, 12)));

            assertEquals(Resultat.SUCCES, second.get(2, TimeUnit.SECONDS));
            libererPremiereLecture.countDown();
            assertEquals(Resultat.SUCCES, premier.get(2, TimeUnit.SECONDS));
            assertEquals(2, enregistrees.size());
        } finally {
            libererPremiereLecture.countDown();
            pool.shutdownNow();
        }
    }

    @Test
    void memeIdentifiantDeTypesDifferentsUtiliseDesVerrousIndependants() throws Exception {
        rendreDisponible(TypeRessource.OUVRIER, "shared");
        rendreDisponible(TypeRessource.TRACTEUR, "shared");
        CountDownLatch premiereLectureEntree = new CountDownLatch(1);
        CountDownLatch libererPremiereLecture = new CountDownLatch(1);
        installerVerificationBloquantType(TypeRessource.OUVRIER, premiereLectureEntree, libererPremiereLecture);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Future<Resultat> ouvrier = pool.submit(() -> executer(demande("shared", TypeRessource.OUVRIER, 8, 12)));
            assertTrue(premiereLectureEntree.await(2, TimeUnit.SECONDS));
            Future<Resultat> tracteur = pool.submit(() -> executer(demande("shared", TypeRessource.TRACTEUR, 8, 12)));

            assertEquals(Resultat.SUCCES, tracteur.get(2, TimeUnit.SECONDS));
            libererPremiereLecture.countDown();
            assertEquals(Resultat.SUCCES, ouvrier.get(2, TimeUnit.SECONDS));
            assertEquals(2, enregistrees.size());
        } finally {
            libererPremiereLecture.countDown();
            pool.shutdownNow();
        }
    }

    @Test
    void uneExceptionPendantLaSauvegardeLibereLeVerrouPourUneAutreRequete() throws Exception {
        rendreDisponible(TypeRessource.TRACTEUR, "r1");
        installerVerificationSansBlocage();
        echouerProchaineSauvegarde.set(true);
        ExecutorService pool = Executors.newSingleThreadExecutor();
        ExecutorService poolReprise = null;
        try {
            Future<?> enEchec = pool.submit(() -> service.creer(demande("r1", TypeRessource.TRACTEUR, 8, 12)));
            java.util.concurrent.ExecutionException exception = org.junit.jupiter.api.Assertions.assertThrows(
                    java.util.concurrent.ExecutionException.class, () -> enEchec.get(2, TimeUnit.SECONDS));
            assertTrue(exception.getCause() instanceof IllegalStateException);
            pool.shutdown();
            assertTrue(pool.awaitTermination(2, TimeUnit.SECONDS));

            poolReprise = Executors.newSingleThreadExecutor();
            Future<Resultat> reprise = poolReprise.submit(() -> executer(demande("r1", TypeRessource.TRACTEUR, 8, 12)));
            assertEquals(Resultat.SUCCES, reprise.get(2, TimeUnit.SECONDS));
            assertEquals(1, enregistrees.size());
        } finally {
            pool.shutdownNow();
            if (poolReprise != null) poolReprise.shutdownNow();
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"ANNULEE", "TERMINEE"})
    void affectationInactiveExistanteNeBloquePasUneCreationActive(String statut) {
        rendreDisponible(TypeRessource.TRACTEUR, "r1");
        Affectation inactive = new Affectation();
        inactive.setId("inactive");
        inactive.setRessourceId("r1");
        inactive.setRessourceType(TypeRessource.TRACTEUR);
        inactive.setDateDebut(debut().withHour(9));
        inactive.setDateFin(debut().withHour(11));
        inactive.setStatut(statut);
        enregistrees.add(inactive);
        installerVerificationSansBlocage();

        assertEquals(Resultat.SUCCES, executer(demande("r1", TypeRessource.TRACTEUR, 8, 12)));
        assertEquals(2, enregistrees.size());
    }

    private void installerVerificationAvecBarrier(CountDownLatch deuxLectures) {
        when(affectationRepository
                .existsByRessourceIdAndRessourceTypeAndStatutNotInAndDateDebutLessThanAndDateFinGreaterThan(
                        anyString(), any(TypeRessource.class), anyList(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenAnswer(invocation -> {
                    boolean conflitAuMomentLecture = conflit(invocation.getArgument(0), invocation.getArgument(1),
                            invocation.getArgument(2), invocation.getArgument(3), invocation.getArgument(4));
                    deuxLectures.countDown();
                    deuxLectures.await(1, TimeUnit.SECONDS);
                    return conflitAuMomentLecture;
                });
    }

    private void installerVerificationBloquantRessource(String ressourceId,
                                                        CountDownLatch entree,
                                                        CountDownLatch liberation) {
        installerVerificationAvecAnswer((id, type, inactifs, debut, fin) -> {
            if (ressourceId.equals(id)) {
                entree.countDown();
                liberation.await(3, TimeUnit.SECONDS);
            }
            return conflit(id, type, inactifs, debut, fin);
        });
    }

    private void installerVerificationBloquantType(TypeRessource typeBloque,
                                                   CountDownLatch entree,
                                                   CountDownLatch liberation) {
        installerVerificationAvecAnswer((id, type, inactifs, debut, fin) -> {
            if (typeBloque == type) {
                entree.countDown();
                liberation.await(3, TimeUnit.SECONDS);
            }
            return conflit(id, type, inactifs, debut, fin);
        });
    }

    private void installerVerificationSansBlocage() {
        installerVerificationAvecAnswer(this::conflit);
    }

    private void installerVerificationAvecAnswer(Verification verification) {
        when(affectationRepository
                .existsByRessourceIdAndRessourceTypeAndStatutNotInAndDateDebutLessThanAndDateFinGreaterThan(
                        anyString(), any(TypeRessource.class), anyList(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenAnswer(invocation -> verification.verifier(invocation.getArgument(0), invocation.getArgument(1),
                        invocation.getArgument(2), invocation.getArgument(3), invocation.getArgument(4)));
    }

    private boolean conflit(String id, TypeRessource type, List<String> inactifs,
                            LocalDateTime dateFinDemandee, LocalDateTime dateDebutDemandee) {
        return enregistrees.stream().anyMatch(affectation -> id.equals(affectation.getRessourceId())
                && type == affectation.getRessourceType()
                && !inactifs.contains(affectation.getStatut())
                && affectation.getDateDebut().isBefore(dateFinDemandee)
                && affectation.getDateFin().isAfter(dateDebutDemandee));
    }

    private void rendreDisponible(TypeRessource type, String id) {
        switch (type) {
            case OUVRIER -> {
                Ouvrier ressource = new Ouvrier();
                ressource.setId(id);
                ressource.setDisponibilite(true);
                when(ouvrierRepository.findById(id)).thenReturn(Optional.of(ressource));
            }
            case TRACTEUR -> {
                Tracteur ressource = new Tracteur();
                ressource.setId(id);
                ressource.setDisponibilite(true);
                when(tracteurRepository.findById(id)).thenReturn(Optional.of(ressource));
            }
            case BENNE -> {
                Benne ressource = new Benne();
                ressource.setId(id);
                ressource.setDisponibilite(true);
                when(benneRepository.findById(id)).thenReturn(Optional.of(ressource));
            }
            case MATERIEL -> {
                Materiel ressource = new Materiel();
                ressource.setId(id);
                ressource.setDisponibilite(true);
                when(materielRepository.findById(id)).thenReturn(Optional.of(ressource));
            }
        }
    }

    private Future<Integer> soumettreHttp(ExecutorService pool, CountDownLatch prets, CountDownLatch depart,
                                          String contenu) {
        return pool.submit(() -> {
            prets.countDown();
            depart.await();
            return mockMvc.perform(post("/api/affectations")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(contenu))
                    .andReturn().getResponse().getStatus();
        });
    }

    private String requeteJson(String id, String type, int heureDebut, int heureFin) {
        return """
                {"ressourceId":"%s","ressourceType":"%s","tourneeId":"tournee",
                 "dateDebut":"2026-11-25T%02d:00:00","dateFin":"2026-11-25T%02d:00:00","statut":"PLANIFIEE"}
                """.formatted(id, type, heureDebut, heureFin);
    }

    private Resultat executer(AffectationRequest demande) {
        try {
            service.creer(demande);
            return Resultat.SUCCES;
        } catch (BusinessConflictException conflit) {
            return Resultat.CONFLIT;
        }
    }

    private AffectationRequest demande(String id, TypeRessource type, int heureDebut, int heureFin) {
        return new AffectationRequest(id, type, "tournee", debut().withHour(heureDebut),
                debut().withHour(heureFin), "PLANIFIEE");
    }

    private LocalDateTime debut() { return LocalDateTime.parse("2026-11-25T00:00:00"); }

    @FunctionalInterface
    private interface Verification {
        boolean verifier(String id, TypeRessource type, List<String> inactifs,
                         LocalDateTime dateFin, LocalDateTime dateDebut) throws Exception;
    }

    private enum Resultat { SUCCES, CONFLIT }
}
