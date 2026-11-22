package tn.oliveconnect.m4_carte.service;

import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.oliveconnect.m4_carte.dto.DelaiCollecteResponse;
import tn.oliveconnect.m4_carte.dto.RemplissageBennesResponse;
import tn.oliveconnect.m4_carte.dto.RendementVergerResponse;
import tn.oliveconnect.m4_carte.repository.DashboardRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    DashboardRepository dashboardRepository;

    @InjectMocks
    DashboardService service;

    @Test
    void rendementEstArrondiADeuxDecimales() {
        Document d = new Document("vergerId", "v1").append("nomVerger", "Verger Sfax Nord")
                .append("superficie", 12.5).append("quantiteRecoltee", 5000.0).append("poidsRecu", 4875.126)
                .append("nombreTournees", 1).append("rendementKgParHa", 390.01008).append("tauxReception", 97.5025);
        when(dashboardRepository.rendementParVerger()).thenReturn(List.of(d));

        List<RendementVergerResponse> res = service.rendement();

        assertEquals(1, res.size());
        RendementVergerResponse r = res.get(0);
        assertEquals("v1", r.vergerId());
        assertEquals("Verger Sfax Nord", r.nomVerger());
        assertEquals(12.5, r.superficie());
        assertEquals(5000.0, r.quantiteRecolteeKg());
        assertEquals(4875.13, r.poidsRecuPressoirKg());
        assertEquals(390.01, r.rendementKgParHa());
        assertEquals(97.5, r.tauxReceptionPourcent());
        assertEquals(1, r.nombreTournees());
    }

    @Test
    void rendementSansDonneesRenvoieUneListeVide() {
        when(dashboardRepository.rendementParVerger()).thenReturn(List.of());

        assertTrue(service.rendement().isEmpty());
    }

    @Test
    void rendementAvecSuperficieInconnueLaisseLeRendementNul() {
        Document d = new Document("vergerId", "v9").append("quantiteRecoltee", 100.0).append("poidsRecu", 90.0)
                .append("nombreTournees", 1).append("rendementKgParHa", null).append("tauxReception", 90.0);
        when(dashboardRepository.rendementParVerger()).thenReturn(List.of(d));

        RendementVergerResponse r = service.rendement().get(0);

        assertNull(r.rendementKgParHa());
        assertNull(r.nomVerger());
        assertEquals(90.0, r.tauxReceptionPourcent());
    }

    @Test
    void delaisLitLeResultatDuFacet() {
        Document facet = new Document()
                .append("global", List.of(new Document("_id", null).append("moyenne", 3.456)
                        .append("min", 1.0).append("max", 6.0).append("n", 4)))
                .append("parType", List.of(
                        new Document("_id", false).append("moyenne", 4.0).append("n", 3),
                        new Document("_id", true).append("moyenne", 1.0).append("n", 1)))
                .append("parVerger", List.of(
                        new Document("vergerId", "v1").append("nomVerger", "Verger Sfax Nord")
                                .append("moyenne", 5.0).append("n", 2)));
        when(dashboardRepository.delaisCollecte()).thenReturn(facet);

        DelaiCollecteResponse r = service.delais();

        assertEquals(4, r.nombreRecoltes());
        assertEquals(3.46, r.delaiMoyenJours());
        assertEquals(1.0, r.delaiMinJours());
        assertEquals(6.0, r.delaiMaxJours());
        assertEquals(2, r.parType().size());
        assertEquals(false, r.parType().get(0).urgente());
        assertEquals(true, r.parType().get(1).urgente());
        assertEquals(1, r.parType().get(1).nombreRecoltes());
        assertEquals("Verger Sfax Nord", r.parVerger().get(0).nomVerger());
        assertEquals(5.0, r.parVerger().get(0).delaiMoyenJours());
    }

    @Test
    void delaisSansDonneesRenvoieDesZeros() {
        when(dashboardRepository.delaisCollecte()).thenReturn(new Document());

        DelaiCollecteResponse r = service.delais();

        assertEquals(0, r.nombreRecoltes());
        assertNull(r.delaiMoyenJours());
        assertTrue(r.parType().isEmpty());
        assertTrue(r.parVerger().isEmpty());
    }

    @Test
    void remplissageCalculeLaMoyenneGlobalePondereeParLeNombreDeReceptions() {
        Document a = new Document("benneId", "b1").append("immatriculation", "TUN-1001").append("capaciteKg", 3000.0)
                .append("nombreReceptions", 2).append("poidsMoyenKg", 1500.0)
                .append("tauxRemplissageMoyen", 50.0).append("tauxRemplissageMax", 60.0);
        Document b = new Document("benneId", "b2").append("immatriculation", "TUN-1002").append("capaciteKg", 3000.0)
                .append("nombreReceptions", 1).append("poidsMoyenKg", 2400.0)
                .append("tauxRemplissageMoyen", 80.0).append("tauxRemplissageMax", 80.0);
        when(dashboardRepository.remplissageParBenne()).thenReturn(List.of(a, b));

        RemplissageBennesResponse r = service.remplissageBennes();

        assertEquals(3, r.nombreReceptions());
        assertEquals(60.0, r.tauxRemplissageMoyenPourcent());
        assertEquals(2, r.parBenne().size());
        assertEquals("TUN-1001", r.parBenne().get(0).immatriculation());
        assertEquals(50.0, r.parBenne().get(0).tauxRemplissageMoyenPourcent());
    }

    @Test
    void remplissageSansReceptionRenvoieUnTauxNul() {
        when(dashboardRepository.remplissageParBenne()).thenReturn(List.of());

        RemplissageBennesResponse r = service.remplissageBennes();

        assertEquals(0, r.nombreReceptions());
        assertNull(r.tauxRemplissageMoyenPourcent());
        assertTrue(r.parBenne().isEmpty());
    }
}
