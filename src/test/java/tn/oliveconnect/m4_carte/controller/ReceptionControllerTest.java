package tn.oliveconnect.m4_carte.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import tn.oliveconnect.common.dto.PageResponse;
import tn.oliveconnect.common.exception.BusinessConflictException;
import tn.oliveconnect.common.exception.NotFoundException;
import tn.oliveconnect.common.security.JwtService;
import tn.oliveconnect.common.security.RestSecurityErrorHandler;
import tn.oliveconnect.common.security.SecurityConfig;
import tn.oliveconnect.m4_carte.dto.ReceptionRequest;
import tn.oliveconnect.m4_carte.dto.ReceptionResponse;
import tn.oliveconnect.m4_carte.entity.QualiteReception;
import tn.oliveconnect.m4_carte.service.ReceptionService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReceptionController.class)
@Import({SecurityConfig.class, JwtService.class, RestSecurityErrorHandler.class})
class ReceptionControllerTest {

    private static final String CORPS_CADRAGE =
            "{\"tourneeId\":\"t7\",\"benneId\":\"b3\",\"pressoirId\":\"p1\",\"poids\":2400.5,\"qualite\":\"BONNE\"}";

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ReceptionService receptionService;

    @Test
    @WithMockUser(username = "u-pressoir", roles = "RESPONSABLE_PRESSOIR")
    void receptionEnregistreeRenvoie201() throws Exception {
        when(receptionService.enregistrer(any(ReceptionRequest.class), eq("u-pressoir")))
                .thenReturn(new ReceptionResponse("rc9", "t7", "b3", "p1",
                        LocalDateTime.of(2026, 11, 19, 10, 0), 2400.5, QualiteReception.BONNE));

        mockMvc.perform(post("/api/receptions").contentType(MediaType.APPLICATION_JSON).content(CORPS_CADRAGE))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("rc9"))
                .andExpect(jsonPath("$.qualite").value("BONNE"))
                .andExpect(jsonPath("$.poids").value(2400.5));
    }

    @Test
    @WithMockUser(username = "u-pressoir", roles = "RESPONSABLE_PRESSOIR")
    void poidsNegatifRenvoie400() throws Exception {
        String corps = CORPS_CADRAGE.replace("2400.5", "-5");

        mockMvc.perform(post("/api/receptions").contentType(MediaType.APPLICATION_JSON).content(corps))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.path").value("/api/receptions"));
        verify(receptionService, never()).enregistrer(any(), any());
    }

    @Test
    @WithMockUser(username = "u-pressoir", roles = "RESPONSABLE_PRESSOIR")
    void qualiteInconnueRenvoie400() throws Exception {
        String corps = CORPS_CADRAGE.replace("BONNE", "SUPER");

        mockMvc.perform(post("/api/receptions").contentType(MediaType.APPLICATION_JSON).content(corps))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "u-pressoir", roles = "RESPONSABLE_PRESSOIR")
    void champObligatoireManquantRenvoie400() throws Exception {
        mockMvc.perform(post("/api/receptions").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tourneeId\":\"t7\",\"poids\":10,\"qualite\":\"BONNE\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "u-pressoir", roles = "RESPONSABLE_PRESSOIR")
    void benneInconnueRenvoie404() throws Exception {
        when(receptionService.enregistrer(any(ReceptionRequest.class), any()))
                .thenThrow(new NotFoundException("Benne introuvable : b3"));

        mockMvc.perform(post("/api/receptions").contentType(MediaType.APPLICATION_JSON).content(CORPS_CADRAGE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Benne introuvable : b3"));
    }

    @Test
    @WithMockUser(username = "u-pressoir", roles = "RESPONSABLE_PRESSOIR")
    void tourneeNonDemarreeRenvoie409() throws Exception {
        when(receptionService.enregistrer(any(ReceptionRequest.class), any()))
                .thenThrow(new BusinessConflictException("Réception impossible"));

        mockMvc.perform(post("/api/receptions").contentType(MediaType.APPLICATION_JSON).content(CORPS_CADRAGE))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("CONFLICT"));
    }

    @Test
    @WithMockUser(username = "u-agri1", roles = "AGRICULTEUR")
    void autreRoleRenvoie403() throws Exception {
        mockMvc.perform(post("/api/receptions").contentType(MediaType.APPLICATION_JSON).content(CORPS_CADRAGE))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
        verify(receptionService, never()).enregistrer(any(), any());
    }

    @Test
    void sansAuthentificationRenvoie401() throws Exception {
        mockMvc.perform(post("/api/receptions").contentType(MediaType.APPLICATION_JSON).content(CORPS_CADRAGE))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @WithMockUser(username = "u-agri1", roles = "AGRICULTEUR")
    void consultationOuverteATousLesProfilsConnectes() throws Exception {
        when(receptionService.lister("t7", 0, 20)).thenReturn(new PageResponse<>(List.of(
                new ReceptionResponse("rc1", "t7", "b3", "p1", LocalDateTime.of(2026, 11, 18, 9, 0), 100.0,
                        QualiteReception.EXCELLENTE)), 0, 20, 1));

        mockMvc.perform(get("/api/receptions").param("tourneeId", "t7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value("rc1"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void consultationSansAuthentificationRenvoie401() throws Exception {
        mockMvc.perform(get("/api/receptions")).andExpect(status().isUnauthorized());
    }
}
