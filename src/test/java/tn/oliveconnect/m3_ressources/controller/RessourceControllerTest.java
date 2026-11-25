package tn.oliveconnect.m3_ressources.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tn.oliveconnect.common.security.JwtService;
import tn.oliveconnect.common.security.RestSecurityErrorHandler;
import tn.oliveconnect.common.security.SecurityConfig;
import tn.oliveconnect.common.dto.PageResponse;
import tn.oliveconnect.common.exception.BadRequestException;
import tn.oliveconnect.common.exception.NotFoundException;
import tn.oliveconnect.m3_ressources.dto.BenneRequest;
import tn.oliveconnect.m3_ressources.dto.BenneResponse;
import tn.oliveconnect.m3_ressources.dto.OuvrierRequest;
import tn.oliveconnect.m3_ressources.dto.OuvrierResponse;
import tn.oliveconnect.m3_ressources.dto.TracteurResponse;
import tn.oliveconnect.m3_ressources.dto.MaterielResponse;
import tn.oliveconnect.m3_ressources.dto.CalendrierRessourceResponse;
import tn.oliveconnect.m3_ressources.dto.CalendrierAffectationResponse;
import tn.oliveconnect.m3_ressources.dto.PeriodeCalendrierResponse;
import tn.oliveconnect.m3_ressources.service.BenneService;
import tn.oliveconnect.m3_ressources.service.CalendrierService;
import tn.oliveconnect.m3_ressources.service.MaterielService;
import tn.oliveconnect.m3_ressources.service.OuvrierService;
import tn.oliveconnect.m3_ressources.service.TracteurService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.List;
import java.time.LocalDateTime;

@WebMvcTest({OuvrierController.class, TracteurController.class, BenneController.class, MaterielController.class, CalendrierController.class})
@Import({SecurityConfig.class, JwtService.class, RestSecurityErrorHandler.class})
class RessourceControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean OuvrierService ouvrierService;
    @MockitoBean TracteurService tracteurService;
    @MockitoBean BenneService benneService;
    @MockitoBean MaterielService materielService;
    @MockitoBean CalendrierService calendrierService;

    @Test
    @WithMockUser(username = "logistique", roles = "RESPONSABLE_LOGISTIQUE")
    void creationOuvrierRenvoie201EtUnDto() throws Exception {
        when(ouvrierService.creer(any(OuvrierRequest.class)))
                .thenReturn(new OuvrierResponse("o1", "Olivier", true, "Récolte", true));

        mockMvc.perform(post("/api/ressources/ouvriers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nom":"Olivier","disponibilite":true,"specialite":"Récolte","saisonnier":true}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("o1"))
                .andExpect(jsonPath("$.specialite").value("Récolte"));
    }

    @Test
    @WithMockUser(username = "logistique", roles = "RESPONSABLE_LOGISTIQUE")
    void capaciteBenneNulleRenvoie400() throws Exception {
        mockMvc.perform(post("/api/ressources/bennes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nom":"Benne","disponibilite":true,"capacite":0,"immatriculation":"123TU456"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @WithMockUser(username = "collecte", roles = "RESPONSABLE_COLLECTE")
    void roleCollecteNePeutPasGererLesRessources() throws Exception {
        mockMvc.perform(get("/api/ressources/ouvriers")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "logistique", roles = "RESPONSABLE_LOGISTIQUE")
    void listeLesStocksBasAvecLeRoleLogistique() throws Exception {
        when(materielService.listerStocksBas()).thenReturn(List.of(
                new tn.oliveconnect.m3_ressources.dto.MaterielResponse("m1", "Filets", true, "filet", 2, 5)));

        mockMvc.perform(get("/api/ressources/materiels/stock-bas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("m1"))
                .andExpect(jsonPath("$[0].nom").value("Filets"))
                .andExpect(jsonPath("$[0].type").value("filet"))
                .andExpect(jsonPath("$[0].quantiteStock").value(2))
                .andExpect(jsonPath("$[0].seuilMinimum").value(5));
    }

    @Test
    @WithMockUser(username = "collecte", roles = "RESPONSABLE_COLLECTE")
    void stockBasEstReserveAuResponsableLogistique() throws Exception {
        mockMvc.perform(get("/api/ressources/materiels/stock-bas"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "collecte", roles = "RESPONSABLE_COLLECTE")
    void rechercheDisponibilitesPourLesQuatreTypesAvecLeRoleCollecte() throws Exception {
        when(ouvrierService.disponibles(any(), any(), anyInt(), anyInt()))
                .thenReturn(new PageResponse<>(List.of(new OuvrierResponse("o1", "Olivier", true, "Récolte", true)), 0, 20, 1));
        when(tracteurService.disponibles(any(), any(), anyInt(), anyInt()))
                .thenReturn(new PageResponse<>(List.of(new TracteurResponse("t1", "Tracteur", true, "123TU456", 90.0)), 0, 20, 1));
        when(benneService.disponibles(any(), any(), anyInt(), anyInt()))
                .thenReturn(new PageResponse<>(List.of(new BenneResponse("b1", "Benne", true, 1500.0, "789TU456")), 0, 20, 1));
        when(materielService.disponibles(any(), any(), anyInt(), anyInt()))
                .thenReturn(new PageResponse<>(List.of(new MaterielResponse("m1", "Filets", true, "filet", 8, 3)), 0, 20, 1));

        String periode = "?debut=2026-03-01T08:00:00&fin=2026-03-01T18:00:00";
        mockMvc.perform(get("/api/ressources/ouvriers/disponibles" + periode)).andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].disponibilite").value(true));
        mockMvc.perform(get("/api/ressources/tracteurs/disponibles" + periode)).andExpect(status().isOk());
        mockMvc.perform(get("/api/ressources/bennes/disponibles" + periode)).andExpect(status().isOk());
        mockMvc.perform(get("/api/ressources/materiels/disponibles" + periode)).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "logistique", roles = "RESPONSABLE_LOGISTIQUE")
    void rejettePeriodeInverseeParametresManquantsEtDateNonIso() throws Exception {
        when(ouvrierService.disponibles(any(), any(), anyInt(), anyInt())).thenAnswer(invocation -> {
            LocalDateTime debut = invocation.getArgument(0);
            LocalDateTime fin = invocation.getArgument(1);
            if (!debut.isBefore(fin)) {
                throw new BadRequestException("La date de début doit être antérieure à la date de fin");
            }
            return new PageResponse<>(List.of(), 0, 20, 0);
        });
        mockMvc.perform(get("/api/ressources/ouvriers/disponibles")
                        .param("debut", "2026-03-01T18:00:00").param("fin", "2026-03-01T08:00:00"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        mockMvc.perform(get("/api/ressources/ouvriers/disponibles").param("debut", "2026-03-01T08:00:00"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/ressources/ouvriers/disponibles")
                        .param("debut", "01/03/2026").param("fin", "2026-03-01T18:00:00"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "collecte", roles = "RESPONSABLE_COLLECTE")
    void roleCollectePeutConsulterLaDisponibiliteMaisPasLeCrud() throws Exception {
        mockMvc.perform(get("/api/ressources/ouvriers/disponibles")
                        .param("debut", "2026-03-01T08:00:00").param("fin", "2026-03-01T18:00:00"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/ressources/ouvriers")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "utilisateur", roles = "RESPONSABLE_COLLECTE")
    void consulteLeCalendrierAvecEtSansPeriode() throws Exception {
        when(calendrierService.consulter("o1", null, null)).thenReturn(
                new CalendrierRessourceResponse("o1", "Olivier", "ouvrier", true, null, List.of()));
        when(calendrierService.consulter("o1", java.time.LocalDateTime.parse("2026-03-01T08:00:00"),
                java.time.LocalDateTime.parse("2026-03-01T18:00:00"))).thenReturn(
                new CalendrierRessourceResponse("o1", "Olivier", "ouvrier", true,
                        new PeriodeCalendrierResponse(java.time.LocalDateTime.parse("2026-03-01T08:00:00"),
                                java.time.LocalDateTime.parse("2026-03-01T18:00:00")),
                        List.of(new CalendrierAffectationResponse("a1",
                                java.time.LocalDateTime.parse("2026-03-01T10:00:00"),
                                java.time.LocalDateTime.parse("2026-03-01T12:00:00"), "PLANIFIEE"))));

        mockMvc.perform(get("/api/calendrier").param("ressourceId", "o1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("o1"))
                .andExpect(jsonPath("$.nom").value("Olivier"))
                .andExpect(jsonPath("$.typeRessource").value("ouvrier"))
                .andExpect(jsonPath("$.disponibilite").value(true))
                .andExpect(jsonPath("$.periodeDemandee").value(org.hamcrest.Matchers.nullValue()));
        mockMvc.perform(get("/api/calendrier").param("ressourceId", "o1")
                        .param("debut", "2026-03-01T08:00:00").param("fin", "2026-03-01T18:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.periodeDemandee.debut").value("2026-03-01T08:00:00"))
                .andExpect(jsonPath("$.periodeDemandee.fin").value("2026-03-01T18:00:00"))
                .andExpect(jsonPath("$.affectations[0].id").value("a1"))
                .andExpect(jsonPath("$.affectations[0].tourneeId").doesNotExist());
    }

    @Test
    @WithMockUser(username = "utilisateur")
    void calendrierRetourne404EtValideLesParametresDates() throws Exception {
        when(calendrierService.consulter("absent", null, null))
                .thenThrow(new NotFoundException("Ressource introuvable : absent"));

        mockMvc.perform(get("/api/calendrier").param("ressourceId", "absent"))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/calendrier"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/calendrier").param("ressourceId", "o1")
                        .param("debut", "01/03/2026").param("fin", "2026-03-01T18:00:00"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void calendrierRequiertUneAuthentification() throws Exception {
        mockMvc.perform(get("/api/calendrier").param("ressourceId", "o1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void requiertUneAuthentification() throws Exception {
        mockMvc.perform(get("/api/ressources/bennes")).andExpect(status().isUnauthorized());
    }
}
