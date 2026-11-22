package tn.oliveconnect.m4_carte.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.oliveconnect.common.exception.BadRequestException;
import tn.oliveconnect.m4_carte.dto.IngestionBilan;
import tn.oliveconnect.m4_carte.dto.MesureCapteurRequest;
import tn.oliveconnect.m4_carte.entity.DonneeMeteo;
import tn.oliveconnect.m4_carte.entity.ReleveMaturite;
import tn.oliveconnect.m4_carte.repository.DonneeMeteoRepository;
import tn.oliveconnect.m4_carte.repository.ReleveMaturiteRepository;
import tn.oliveconnect.m4_carte.repository.VergerReleveRepository;

import tools.jackson.databind.json.JsonMapper;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MesureIngestionServiceTest {

    @Mock
    DonneeMeteoRepository meteoRepository;
    @Mock
    ReleveMaturiteRepository releveRepository;
    @Mock
    VergerReleveRepository vergerReleveRepository;

    MesureIngestionService service;

    @BeforeEach
    void init() {
        Clock clock = Clock.fixed(Instant.parse("2026-11-19T10:00:00Z"), ZoneId.of("UTC"));
        service = new MesureIngestionService(meteoRepository, releveRepository, vergerReleveRepository,
                JsonMapper.builder().build(), clock);
    }

    @Test
    void mesureValideEstEnregistreeEtImbriqueeDansLeVerger() {
        when(vergerReleveRepository.existe("v1")).thenReturn(true);

        IngestionBilan bilan = service.ingerer(List.of(new MesureCapteurRequest("v1", null, 20.0, 60.0, 0.0, 45.0)));

        assertEquals(new IngestionBilan(1, 0), bilan);
        verify(meteoRepository).save(any(DonneeMeteo.class));
        verify(releveRepository).save(any(ReleveMaturite.class));
        verify(vergerReleveRepository).pousserDernierReleve(eq("v1"), eq(LocalDateTime.of(2026, 11, 19, 10, 0)), eq(45.0));
    }

    @Test
    void mesureSansMaturiteNeToucheNiLesRelevesNiLeVerger() {
        when(vergerReleveRepository.existe("v1")).thenReturn(true);

        service.ingerer(List.of(new MesureCapteurRequest("v1", null, 20.0, null, null, null)));

        verify(meteoRepository).save(any(DonneeMeteo.class));
        verify(releveRepository, never()).save(any());
        verify(vergerReleveRepository, never()).pousserDernierReleve(any(), any(), any());
    }

    @Test
    void humiditeHorsLimitesEstRejetee() {
        IngestionBilan bilan = service.ingerer(List.of(new MesureCapteurRequest("v1", null, 20.0, 150.0, 0.0, 45.0)));

        assertEquals(new IngestionBilan(0, 1), bilan);
        verifyNoInteractions(meteoRepository, releveRepository, vergerReleveRepository);
    }

    @Test
    void maturiteNegativeEtMesureVideSontRejetees() {
        IngestionBilan bilan = service.ingerer(List.of(
                new MesureCapteurRequest("v1", null, null, null, null, -5.0),
                new MesureCapteurRequest("v1", null, null, null, null, null),
                new MesureCapteurRequest(" ", null, 20.0, null, null, null)));

        assertEquals(new IngestionBilan(0, 3), bilan);
        verifyNoInteractions(meteoRepository, releveRepository, vergerReleveRepository);
    }

    @Test
    void vergerInconnuEstRejete() {
        when(vergerReleveRepository.existe("vX")).thenReturn(false);

        IngestionBilan bilan = service.ingerer(List.of(new MesureCapteurRequest("vX", null, 20.0, 50.0, 0.0, 30.0)));

        assertEquals(new IngestionBilan(0, 1), bilan);
        verifyNoInteractions(meteoRepository, releveRepository);
    }

    @Test
    void jsonValideEstIngere() {
        when(vergerReleveRepository.existe("v1")).thenReturn(true);
        String json = "[{\"vergerId\":\"v1\",\"dateReleve\":\"2026-11-19T09:00:00\",\"temperature\":18.5,\"niveauMaturite\":30}]";

        IngestionBilan bilan = service.ingererJson(json);

        assertEquals(new IngestionBilan(1, 0), bilan);
        verify(vergerReleveRepository).pousserDernierReleve(eq("v1"), eq(LocalDateTime.of(2026, 11, 19, 9, 0)), eq(30.0));
    }

    @Test
    void jsonMalformeDonne400() {
        assertThrows(BadRequestException.class, () -> service.ingererJson("{pas du json"));
    }
}
