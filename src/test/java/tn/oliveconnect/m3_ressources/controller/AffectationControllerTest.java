package tn.oliveconnect.m3_ressources.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tn.oliveconnect.common.dto.PageResponse;
import tn.oliveconnect.common.exception.BusinessConflictException;
import tn.oliveconnect.common.exception.NotFoundException;
import tn.oliveconnect.common.security.JwtService;
import tn.oliveconnect.common.security.RestSecurityErrorHandler;
import tn.oliveconnect.common.security.SecurityConfig;
import tn.oliveconnect.m3_ressources.dto.AffectationResponse;
import tn.oliveconnect.m3_ressources.entity.TypeRessource;
import tn.oliveconnect.m3_ressources.service.AffectationService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AffectationController.class)
@Import({SecurityConfig.class, JwtService.class, RestSecurityErrorHandler.class})
class AffectationControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean AffectationService service;

    @Test
    @WithMockUser(username = "logistique", roles = "RESPONSABLE_LOGISTIQUE")
    void logisticienCreeUneAffectationEtConsulteLaListe() throws Exception {
        when(service.creer(any())).thenReturn(response());
        when(service.listerParRessource("r1", TypeRessource.TRACTEUR, 0, 20))
                .thenReturn(new PageResponse<>(List.of(response()), 0, 20, 1));

        mockMvc.perform(post("/api/affectations").contentType(MediaType.APPLICATION_JSON).content(requestJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ressourceType").value("TRACTEUR"))
                .andExpect(jsonPath("$.tourneeId").value("t456"));
        mockMvc.perform(get("/api/affectations").param("ressourceId", "r1").param("ressourceType", "TRACTEUR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser(username = "collecte", roles = "RESPONSABLE_COLLECTE")
    void responsableCollectePeutCreerMaisPasSupprimer() throws Exception {
        when(service.creer(any())).thenReturn(response());
        mockMvc.perform(post("/api/affectations").contentType(MediaType.APPLICATION_JSON).content(requestJson()))
                .andExpect(status().isCreated());
        mockMvc.perform(delete("/api/affectations/a1")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "logistique", roles = "RESPONSABLE_LOGISTIQUE")
    void suppressionRenvoie204Ou404() throws Exception {
        doThrow(new NotFoundException("Affectation introuvable : absent"))
                .when(service).supprimer("absent");
        mockMvc.perform(delete("/api/affectations/a1")).andExpect(status().isNoContent());
        mockMvc.perform(delete("/api/affectations/absent")).andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "logistique", roles = "RESPONSABLE_LOGISTIQUE")
    void unConflitDAffectationEstExposeEnHttp409() throws Exception {
        when(service.creer(any())).thenThrow(new BusinessConflictException("Chevauchement détecté"));

        mockMvc.perform(post("/api/affectations").contentType(MediaType.APPLICATION_JSON).content(requestJson()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    private AffectationResponse response() {
        return new AffectationResponse("a1", "r1", TypeRessource.TRACTEUR, "t456",
                LocalDateTime.parse("2026-11-25T08:00:00"), LocalDateTime.parse("2026-11-25T12:00:00"), "PLANIFIEE");
    }

    private String requestJson() {
        return """
                {"ressourceId":"r1","ressourceType":"TRACTEUR","tourneeId":"t456",
                 "dateDebut":"2026-11-25T08:00:00","dateFin":"2026-11-25T12:00:00","statut":"PLANIFIEE"}
                """;
    }
}
