package tn.oliveconnect.m3_ressources.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.oliveconnect.common.exception.BadRequestException;
import tn.oliveconnect.common.exception.NotFoundException;
import tn.oliveconnect.m3_ressources.entity.Benne;
import tn.oliveconnect.m3_ressources.entity.Materiel;
import tn.oliveconnect.m3_ressources.entity.Ouvrier;
import tn.oliveconnect.m3_ressources.entity.Tracteur;
import tn.oliveconnect.m3_ressources.entity.TypeRessource;
import tn.oliveconnect.m3_ressources.dto.AffectationResponse;
import tn.oliveconnect.m3_ressources.repository.BenneRepository;
import tn.oliveconnect.m3_ressources.repository.MaterielRepository;
import tn.oliveconnect.m3_ressources.repository.OuvrierRepository;
import tn.oliveconnect.m3_ressources.repository.TracteurRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
class CalendrierServiceTest {

    @Mock OuvrierRepository ouvrierRepository;
    @Mock TracteurRepository tracteurRepository;
    @Mock BenneRepository benneRepository;
    @Mock MaterielRepository materielRepository;
    @Mock AffectationService affectationService;
    @InjectMocks CalendrierService service;

    @Test
    void consulteLesQuatreTypesDeRessource() {
        Ouvrier ouvrier = new Ouvrier();
        ouvrier.setId("o1");
        ouvrier.setNom("Olivier");
        ouvrier.setDisponibilite(true);
        Tracteur tracteur = new Tracteur();
        tracteur.setId("t1");
        tracteur.setNom("Tracteur");
        tracteur.setDisponibilite(false);
        Benne benne = new Benne();
        benne.setId("b1");
        benne.setNom("Benne");
        benne.setDisponibilite(true);
        Materiel materiel = new Materiel();
        materiel.setId("m1");
        materiel.setNom("Filet");
        materiel.setDisponibilite(true);
        when(ouvrierRepository.findById(anyString())).thenAnswer(call -> "o1".equals(call.getArgument(0))
                ? Optional.of(ouvrier) : Optional.empty());
        when(tracteurRepository.findById(anyString())).thenAnswer(call -> "t1".equals(call.getArgument(0))
                ? Optional.of(tracteur) : Optional.empty());
        when(benneRepository.findById(anyString())).thenAnswer(call -> "b1".equals(call.getArgument(0))
                ? Optional.of(benne) : Optional.empty());
        when(materielRepository.findById(anyString())).thenAnswer(call -> "m1".equals(call.getArgument(0))
                ? Optional.of(materiel) : Optional.empty());

        assertEquals("ouvrier", service.consulter("o1", null, null).typeRessource());
        assertEquals(true, service.consulter("o1", null, null).disponibilite());
        assertEquals("tracteur", service.consulter("t1", null, null).typeRessource());
        assertEquals(false, service.consulter("t1", null, null).disponibilite());
        assertEquals("benne", service.consulter("b1", null, null).typeRessource());
        assertEquals("materiel", service.consulter("m1", null, null).typeRessource());
    }

    @Test
    void retourne404PourUnIdentifiantInconnu() {
        assertThrows(NotFoundException.class, () -> service.consulter("inconnu", null, null));
    }

    @Test
    void retourneLaPeriodeDemandeeEtAccepteLaConsultationSansDates() {
        Ouvrier ouvrier = new Ouvrier();
        ouvrier.setId("o1");
        ouvrier.setNom("Olivier");
        ouvrier.setDisponibilite(true);
        when(ouvrierRepository.findById("o1")).thenReturn(Optional.of(ouvrier));
        LocalDateTime debut = LocalDateTime.parse("2026-03-01T08:00:00");
        LocalDateTime fin = LocalDateTime.parse("2026-03-01T18:00:00");

        var avecPeriode = service.consulter("o1", debut, fin);
        var sansPeriode = service.consulter("o1", null, null);

        assertEquals(debut, avecPeriode.periodeDemandee().debut());
        assertEquals(fin, avecPeriode.periodeDemandee().fin());
        assertNull(sansPeriode.periodeDemandee());
    }

    @Test
    void rejettePeriodeInverseEtDatePartielle() {
        LocalDateTime debut = LocalDateTime.parse("2026-03-01T18:00:00");
        LocalDateTime fin = LocalDateTime.parse("2026-03-01T08:00:00");
        assertThrows(BadRequestException.class, () -> service.consulter("o1", debut, fin));
        assertThrows(BadRequestException.class, () -> service.consulter("o1", debut, null));
        assertThrows(BadRequestException.class, () -> service.consulter("o1", null, fin));
    }

    @Test
    void exposeLesAffectationsReellesEtFiltreSurLaPeriodeDemandee() {
        Ouvrier ouvrier = new Ouvrier();
        ouvrier.setId("o1");
        ouvrier.setNom("Olivier");
        ouvrier.setDisponibilite(true);
        when(ouvrierRepository.findById("o1")).thenReturn(Optional.of(ouvrier));
        when(affectationService.listerPourCalendrier("o1", TypeRessource.OUVRIER)).thenReturn(List.of(
                new AffectationResponse("a1", "o1", TypeRessource.OUVRIER, "external-t1",
                        LocalDateTime.parse("2026-03-01T10:00:00"), LocalDateTime.parse("2026-03-01T12:00:00"), "PLANIFIEE"),
                new AffectationResponse("a2", "o1", TypeRessource.OUVRIER, "external-t2",
                        LocalDateTime.parse("2026-03-01T20:00:00"), LocalDateTime.parse("2026-03-01T22:00:00"), "PLANIFIEE")));

        var calendrier = service.consulter("o1", LocalDateTime.parse("2026-03-01T08:00:00"),
                LocalDateTime.parse("2026-03-01T18:00:00"));

        assertEquals(1, calendrier.affectations().size());
        assertEquals("a1", calendrier.affectations().get(0).id());
        assertEquals("PLANIFIEE", calendrier.affectations().get(0).statut());
    }
}
