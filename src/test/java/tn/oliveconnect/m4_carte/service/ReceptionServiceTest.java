package tn.oliveconnect.m4_carte.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import tn.oliveconnect.common.dto.PageResponse;
import tn.oliveconnect.common.exception.BadRequestException;
import tn.oliveconnect.common.exception.BusinessConflictException;
import tn.oliveconnect.common.exception.ForbiddenException;
import tn.oliveconnect.common.exception.NotFoundException;
import tn.oliveconnect.m4_carte.dto.ReceptionRequest;
import tn.oliveconnect.m4_carte.dto.ReceptionResponse;
import tn.oliveconnect.m4_carte.entity.Pressoir;
import tn.oliveconnect.m4_carte.entity.QualiteReception;
import tn.oliveconnect.m4_carte.entity.Reception;
import tn.oliveconnect.m4_carte.integration.BenneReader;
import tn.oliveconnect.m4_carte.integration.BenneRef;
import tn.oliveconnect.m4_carte.integration.TourneeReader;
import tn.oliveconnect.m4_carte.integration.TourneeRef;
import tn.oliveconnect.m4_carte.repository.PressoirRepository;
import tn.oliveconnect.m4_carte.repository.ReceptionRepository;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReceptionServiceTest {

    @Mock
    ReceptionRepository receptionRepository;
    @Mock
    PressoirRepository pressoirRepository;
    @Mock
    TourneeReader tourneeReader;
    @Mock
    BenneReader benneReader;

    final Clock clock = Clock.fixed(Instant.parse("2026-11-19T10:00:00Z"), ZoneId.of("UTC"));

    ReceptionService service;

    @BeforeEach
    void init() {
        service = new ReceptionService(receptionRepository, pressoirRepository, tourneeReader, benneReader, clock);
    }

    private static ReceptionRequest requete() {
        return new ReceptionRequest("t7", "b3", "p1", 2400.5, QualiteReception.BONNE);
    }

    private static Pressoir pressoir(String responsableId) {
        return Pressoir.builder().id("p1").nom("Pressoir").responsableId(responsableId).build();
    }

    // ---- nominal ----

    @Test
    void receptionEnregistreeQuandToutEstValide() {
        when(pressoirRepository.findById("p1")).thenReturn(Optional.of(pressoir("u-pressoir")));
        when(benneReader.findById("b3")).thenReturn(Optional.of(new BenneRef("b3", "TUN-1003", 3000.0)));
        when(tourneeReader.findById("t7")).thenReturn(Optional.of(new TourneeRef("t7", "EN_COURS")));
        when(receptionRepository.save(any(Reception.class))).thenAnswer(inv -> {
            Reception r = inv.getArgument(0);
            r.setId("rc9");
            return r;
        });

        ReceptionResponse res = service.enregistrer(requete(), "u-pressoir");

        assertEquals("rc9", res.id());
        assertEquals("t7", res.tourneeId());
        assertEquals("b3", res.benneId());
        assertEquals("p1", res.pressoirId());
        assertEquals(2400.5, res.poids());
        assertEquals(QualiteReception.BONNE, res.qualite());
        assertEquals(LocalDateTime.of(2026, 11, 19, 10, 0), res.dateReception());
    }

    @Test
    void receptionAccepteePourUneTourneeTerminee() {
        when(pressoirRepository.findById("p1")).thenReturn(Optional.of(pressoir("u-pressoir")));
        when(benneReader.findById("b3")).thenReturn(Optional.of(new BenneRef("b3", "TUN-1003", 3000.0)));
        when(tourneeReader.findById("t7")).thenReturn(Optional.of(new TourneeRef("t7", "TERMINEE")));
        when(receptionRepository.save(any(Reception.class))).thenAnswer(inv -> inv.getArgument(0));

        assertEquals(2400.5, service.enregistrer(requete(), "u-pressoir").poids());
    }

    // ---- alternatifs ----

    @Test
    void benneInconnueDonne404() {
        when(pressoirRepository.findById("p1")).thenReturn(Optional.of(pressoir("u-pressoir")));
        when(benneReader.findById("b3")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.enregistrer(requete(), "u-pressoir"));
        verify(receptionRepository, never()).save(any());
    }

    @Test
    void pressoirInconnuDonne404() {
        when(pressoirRepository.findById("p1")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.enregistrer(requete(), "u-pressoir"));
        verify(receptionRepository, never()).save(any());
    }

    @Test
    void tourneeInconnueDonne404() {
        when(pressoirRepository.findById("p1")).thenReturn(Optional.of(pressoir("u-pressoir")));
        when(benneReader.findById("b3")).thenReturn(Optional.of(new BenneRef("b3", "TUN-1003", 3000.0)));
        when(tourneeReader.findById("t7")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.enregistrer(requete(), "u-pressoir"));
        verify(receptionRepository, never()).save(any());
    }

    @Test
    void pressoirGereParUnAutreResponsableDonne403() {
        when(pressoirRepository.findById("p1")).thenReturn(Optional.of(pressoir("u-pressoir")));

        assertThrows(ForbiddenException.class, () -> service.enregistrer(requete(), "u-autre"));
        verify(receptionRepository, never()).save(any());
    }

    @Test
    void tourneeNonDemarreeDonne409() {
        when(pressoirRepository.findById("p1")).thenReturn(Optional.of(pressoir("u-pressoir")));
        when(benneReader.findById("b3")).thenReturn(Optional.of(new BenneRef("b3", "TUN-1003", 3000.0)));
        when(tourneeReader.findById("t7")).thenReturn(Optional.of(new TourneeRef("t7", "PLANIFIEE")));

        assertThrows(BusinessConflictException.class, () -> service.enregistrer(requete(), "u-pressoir"));
        verify(receptionRepository, never()).save(any());
    }

    @Test
    void tourneeAnnuleeDonne409() {
        when(pressoirRepository.findById("p1")).thenReturn(Optional.of(pressoir("u-pressoir")));
        when(benneReader.findById("b3")).thenReturn(Optional.of(new BenneRef("b3", "TUN-1003", 3000.0)));
        when(tourneeReader.findById("t7")).thenReturn(Optional.of(new TourneeRef("t7", "ANNULEE")));

        assertThrows(BusinessConflictException.class, () -> service.enregistrer(requete(), "u-pressoir"));
    }

    // ---- liste ----

    @Test
    void listeFiltreeParTourneeEstPaginee() {
        Reception r = Reception.builder().id("rc1").tourneeId("t7").benneId("b3").pressoirId("p1")
                .poids(100.0).qualite(QualiteReception.BONNE).dateReception(LocalDateTime.of(2026, 11, 18, 9, 0)).build();
        when(receptionRepository.findByTourneeId(any(String.class), any(Pageable.class)))
                .thenAnswer(inv -> new PageImpl<>(List.of(r), inv.getArgument(1), 1));

        PageResponse<ReceptionResponse> page = service.lister("t7", 0, 20);

        assertEquals(1, page.totalElements());
        assertEquals(0, page.page());
        assertEquals(20, page.size());
        assertEquals("rc1", page.content().get(0).id());
    }

    @Test
    void listeSansTourneeLitToutesLesReceptions() {
        when(receptionRepository.findAll(any(Pageable.class)))
                .thenAnswer(inv -> new PageImpl<Reception>(List.of(), inv.getArgument(0), 0));

        assertEquals(0, service.lister(null, 0, 20).totalElements());
        verify(receptionRepository, never()).findByTourneeId(any(), any());
    }

    @Test
    void paginationInvalideDonne400() {
        assertThrows(BadRequestException.class, () -> service.lister(null, -1, 20));
        assertThrows(BadRequestException.class, () -> service.lister(null, 0, 0));
        assertThrows(BadRequestException.class, () -> service.lister(null, 0, 101));
    }
}
