package tn.oliveconnect.m3_ressources.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import tn.oliveconnect.common.exception.BadRequestException;
import tn.oliveconnect.common.exception.BusinessConflictException;
import tn.oliveconnect.common.exception.NotFoundException;
import tn.oliveconnect.m3_ressources.dto.AffectationRequest;
import tn.oliveconnect.m3_ressources.entity.Affectation;
import tn.oliveconnect.m3_ressources.entity.Benne;
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
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AffectationServiceTest {

    @Mock AffectationRepository affectationRepository;
    @Mock OuvrierRepository ouvrierRepository;
    @Mock TracteurRepository tracteurRepository;
    @Mock BenneRepository benneRepository;
    @Mock MaterielRepository materielRepository;
    @Spy VerrouAffectationRessource verrouAffectationRessource = new VerrouAffectationRessource();
    @InjectMocks AffectationService service;

    @Test
    void creeUneAffectationValideSansVerifierLaTournee() {
        Tracteur tracteur = tracteur("r1", true);
        when(tracteurRepository.findById("r1")).thenReturn(Optional.of(tracteur));
        when(affectationRepository.existsByRessourceIdAndRessourceTypeAndStatutNotInAndDateDebutLessThanAndDateFinGreaterThan(
                "r1", TypeRessource.TRACTEUR, List.of("ANNULEE", "TERMINEE"), fin(), debut())).thenReturn(false);
        stubsSaveWithId();

        var response = service.creer(request("r1", TypeRessource.TRACTEUR, debut(), fin(), " t456 "));

        assertEquals("a1", response.id());
        assertEquals("t456", response.tourneeId());
        assertEquals("PLANIFIEE", response.statut());
        verify(affectationRepository).save(any(Affectation.class));
    }

    @Test
    void rejetteUneRessourceInexistanteOuIndisponible() {
        when(tracteurRepository.findById("absent")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class,
                () -> service.creer(request("absent", TypeRessource.TRACTEUR, debut(), fin(), "t1")));

        when(tracteurRepository.findById("r2")).thenReturn(Optional.of(tracteur("r2", false)));
        assertThrows(BusinessConflictException.class,
                () -> service.creer(request("r2", TypeRessource.TRACTEUR, debut(), fin(), "t1")));
        verify(affectationRepository, never())
                .existsByRessourceIdAndRessourceTypeAndStatutNotInAndDateDebutLessThanAndDateFinGreaterThan(
                        any(), any(), anyList(), any(), any());
    }

    @Test
    void rejetteUnePeriodeInvalide() {
        assertThrows(BadRequestException.class,
                () -> service.creer(request("r1", TypeRessource.TRACTEUR, fin(), debut(), "t1")));
    }

    @Test
    void rejetteUnChevauchementPourLaMemeRessource() {
        when(tracteurRepository.findById("r1")).thenReturn(Optional.of(tracteur("r1", true)));
        when(affectationRepository.existsByRessourceIdAndRessourceTypeAndStatutNotInAndDateDebutLessThanAndDateFinGreaterThan(
                "r1", TypeRessource.TRACTEUR, List.of("ANNULEE", "TERMINEE"), fin(), debut())).thenReturn(true);

        assertThrows(BusinessConflictException.class,
                () -> service.creer(request("r1", TypeRessource.TRACTEUR, debut(), fin(), "t1")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ANNULEE", "TERMINEE"})
    void uneAffectationInactiveNeReservePasLeCreneau(String statut) {
        when(tracteurRepository.findById("r1")).thenReturn(Optional.of(tracteur("r1", true)));
        stubsSaveWithId();
        AffectationRequest annulee = new AffectationRequest("r1", TypeRessource.TRACTEUR, "t1",
                debut(), fin(), statut);

        var response = service.creer(annulee);

        assertEquals(statut, response.statut());
        verify(affectationRepository, never())
                .existsByRessourceIdAndRessourceTypeAndStatutNotInAndDateDebutLessThanAndDateFinGreaterThan(
                        any(), any(), anyList(), any(), any());
    }

    @Test
    void accepteDeuxPeriodesAdjacentesEtDeuxRessourcesDistinctes() {
        when(tracteurRepository.findById(any())).thenAnswer(invocation ->
                Optional.of(tracteur(invocation.getArgument(0), true)));
        when(affectationRepository.existsByRessourceIdAndRessourceTypeAndStatutNotInAndDateDebutLessThanAndDateFinGreaterThan(
                any(), any(), anyList(), any(), any())).thenReturn(false);
        stubsSaveWithId();

        assertEquals("a1", service.creer(request("r1", TypeRessource.TRACTEUR, debut(), fin(), "t1")).id());
        assertEquals("a1", service.creer(request("r2", TypeRessource.TRACTEUR, fin(), fin().plusHours(2), "t2")).id());
        assertEquals("a1", service.creer(request("r3", TypeRessource.TRACTEUR, debut(), fin(), "t3")).id());
        verify(affectationRepository).existsByRessourceIdAndRessourceTypeAndStatutNotInAndDateDebutLessThanAndDateFinGreaterThan(
                "r2", TypeRessource.TRACTEUR, List.of("ANNULEE", "TERMINEE"), fin().plusHours(2), fin());
    }

    @Test
    void neConfondPasDeuxTypesQuiPartagentLeMemeIdentifiant() {
        when(tracteurRepository.findById("shared")).thenReturn(Optional.of(tracteur("shared", true)));
        when(affectationRepository.existsByRessourceIdAndRessourceTypeAndStatutNotInAndDateDebutLessThanAndDateFinGreaterThan(
                "shared", TypeRessource.TRACTEUR, List.of("ANNULEE", "TERMINEE"), fin(), debut())).thenReturn(false);
        stubsSaveWithId();

        service.creer(request("shared", TypeRessource.TRACTEUR, debut(), fin(), "t1"));

        verify(tracteurRepository).findById("shared");
        verify(ouvrierRepository, never()).findById(any());
        verify(affectationRepository).existsByRessourceIdAndRessourceTypeAndStatutNotInAndDateDebutLessThanAndDateFinGreaterThan(
                "shared", TypeRessource.TRACTEUR, List.of("ANNULEE", "TERMINEE"), fin(), debut());
    }

    @Test
    void rechercheLesConflitsAvecTypeExactEtBornesStrictes() {
        Affectation affectation = new Affectation();
        affectation.setRessourceId("shared");
        affectation.setRessourceType(TypeRessource.OUVRIER);
        when(affectationRepository
                .findByRessourceTypeAndStatutNotInAndDateDebutLessThanAndDateFinGreaterThan(
                        TypeRessource.OUVRIER, List.of("ANNULEE", "TERMINEE"), fin(), debut()))
                .thenReturn(List.of(affectation));

        Set<String> result = service.ressourcesEnConflit(TypeRessource.OUVRIER, debut(), fin());

        assertEquals(Set.of("shared"), result);
        verify(affectationRepository)
                .findByRessourceTypeAndStatutNotInAndDateDebutLessThanAndDateFinGreaterThan(
                        TypeRessource.OUVRIER, List.of("ANNULEE", "TERMINEE"), fin(), debut());
    }

    @Test
    void supprimeUneAffectationEtRetourne404SiInconnue() {
        when(affectationRepository.findById("a1")).thenReturn(Optional.of(new Affectation()));
        service.supprimer("a1");
        verify(affectationRepository).deleteById("a1");

        when(affectationRepository.findById("absent")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.supprimer("absent"));
    }

    @Test
    void retourneUneListePagineePourUneRessourceEtSonType() {
        Affectation value = new Affectation();
        value.setId("a1");
        value.setRessourceId("r1");
        value.setRessourceType(TypeRessource.BENNE);
        value.setTourneeId("t1");
        value.setDateDebut(debut());
        value.setDateFin(fin());
        value.setStatut("PLANIFIEE");
        when(affectationRepository.findByRessourceIdAndRessourceType("r1", TypeRessource.BENNE,
                PageRequest.of(0, 20, org.springframework.data.domain.Sort.by("dateDebut"))))
                .thenReturn(new PageImpl<>(List.of(value)));

        var result = service.listerParRessource("r1", TypeRessource.BENNE, 0, 20);

        assertEquals(1, result.totalElements());
        assertEquals(TypeRessource.BENNE, result.content().get(0).ressourceType());
    }

    private void stubsSaveWithId() {
        when(affectationRepository.save(any(Affectation.class))).thenAnswer(invocation -> {
            Affectation value = invocation.getArgument(0);
            value.setId("a1");
            return value;
        });
    }

    private AffectationRequest request(String id, TypeRessource type, LocalDateTime debut,
                                       LocalDateTime fin, String tourneeId) {
        return new AffectationRequest(id, type, tourneeId, debut, fin, "PLANIFIEE");
    }

    private Tracteur tracteur(String id, boolean disponibilite) {
        Tracteur value = new Tracteur();
        value.setId(id);
        value.setDisponibilite(disponibilite);
        return value;
    }

    private LocalDateTime debut() { return LocalDateTime.parse("2026-11-25T08:00:00"); }
    private LocalDateTime fin() { return LocalDateTime.parse("2026-11-25T12:00:00"); }
}
