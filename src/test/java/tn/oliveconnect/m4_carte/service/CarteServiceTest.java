package tn.oliveconnect.m4_carte.service;

import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.oliveconnect.m4_carte.dto.CartePubliqueVergerResponse;
import tn.oliveconnect.m4_carte.dto.CarteVergerResponse;
import tn.oliveconnect.m4_carte.repository.CarteRepository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CarteServiceTest {

    @Mock
    CarteRepository carteRepository;

    @InjectMocks
    CarteService service;

    private static Date date(int jour) {
        return Date.from(LocalDateTime.of(2026, 11, jour, 9, 0).atZone(ZoneId.systemDefault()).toInstant());
    }

    private static Document vergerAvecPosition() {
        return new Document("_id", "v1").append("nom", "Verger A").append("adresse", "Sfax")
                .append("superficie", 12.5).append("statut", "EN_RECOLTE")
                .append("location", new Document("type", "Point").append("coordinates", List.of(10.76, 34.74)))
                .append("varieteDominanteId", "var1")
                .append("derniersReleves", List.of(
                        new Document("date", date(17)).append("niveauMaturite", 55.0),
                        new Document("date", date(10)).append("niveauMaturite", 40.0)));
    }

    private static Document vergerSansPosition() {
        return new Document("_id", "v2").append("nom", "Sans position");
    }

    @Test
    void cartePubliqueExposeLaPositionLaMaturiteLePlusRecenteEtLeTypeDOlives() {
        when(carteRepository.findVergers()).thenReturn(List.of(vergerAvecPosition(), vergerSansPosition()));
        when(carteRepository.findNomsVarietes()).thenReturn(Map.of("var1", "Chemlali"));

        List<CartePubliqueVergerResponse> res = service.carteVergersPublique();

        assertEquals(1, res.size());
        CartePubliqueVergerResponse v = res.get(0);
        assertEquals("v1", v.id());
        assertEquals("Verger A", v.nom());
        assertEquals(34.74, v.latitude());
        assertEquals(10.76, v.longitude());
        assertEquals(55.0, v.niveauMaturite());
        assertEquals("Chemlali", v.typeOlives());
    }

    @Test
    void typeDOlivesRetombeSurLaVarieteDeLaPremiereParcelle() {
        Document v = new Document("_id", "v3").append("nom", "B")
                .append("location", new Document("coordinates", List.of(9.0, 36.0)))
                .append("parcelles", List.of(new Document("id", "p1").append("varieteId", "var2")));
        when(carteRepository.findVergers()).thenReturn(List.of(v));
        when(carteRepository.findNomsVarietes()).thenReturn(Map.of("var2", "Chétoui"));

        CartePubliqueVergerResponse res = service.carteVergersPublique().get(0);

        assertEquals("Chétoui", res.typeOlives());
        assertNull(res.niveauMaturite());
    }

    @Test
    void coordonneesInvalidesSontIgnorees() {
        Document v = new Document("_id", "v4").append("nom", "Hors limites")
                .append("location", new Document("coordinates", List.of(10.0, 123.0)));
        when(carteRepository.findVergers()).thenReturn(List.of(v));
        when(carteRepository.findNomsVarietes()).thenReturn(Map.of());

        assertEquals(0, service.carteVergersPublique().size());
    }

    @Test
    void carteCompleteAjouteStatutsRelevesEtMeteo() {
        when(carteRepository.findVergers()).thenReturn(List.of(vergerAvecPosition()));
        when(carteRepository.findNomsVarietes()).thenReturn(Map.of("var1", "Chemlali"));
        when(carteRepository.dernierStatutRecoltes()).thenReturn(Map.of("v1", "EN_COURS"));
        when(carteRepository.dernieresMeteo()).thenReturn(Map.of("v1",
                new Document("dateReleve", date(18)).append("temperature", 21.5)
                        .append("humidite", 60.0).append("precipitation", 0.0)));

        List<CarteVergerResponse> res = service.carteVergersComplete();

        assertEquals(1, res.size());
        CarteVergerResponse v = res.get(0);
        assertEquals("EN_RECOLTE", v.statutVerger());
        assertEquals("EN_COURS", v.statutRecolte());
        assertEquals(12.5, v.superficie());
        assertEquals(55.0, v.niveauMaturite());
        assertEquals(2, v.derniersReleves().size());
        assertEquals(40.0, v.derniersReleves().get(0).niveauMaturite()); // ordre chronologique
        assertEquals(21.5, v.meteo().temperature());
    }

    @Test
    void carteCompleteSansRecolteNiMeteoLaisseCesChampsNuls() {
        when(carteRepository.findVergers()).thenReturn(List.of(vergerAvecPosition()));
        when(carteRepository.findNomsVarietes()).thenReturn(Map.of());
        when(carteRepository.dernierStatutRecoltes()).thenReturn(Map.of());
        when(carteRepository.dernieresMeteo()).thenReturn(Map.of());

        CarteVergerResponse v = service.carteVergersComplete().get(0);

        assertNull(v.statutRecolte());
        assertNull(v.meteo());
        assertNull(v.typeOlives());
    }
}
