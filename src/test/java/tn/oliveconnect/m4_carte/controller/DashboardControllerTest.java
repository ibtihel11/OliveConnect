package tn.oliveconnect.m4_carte.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import tn.oliveconnect.common.security.JwtService;
import tn.oliveconnect.common.security.RestSecurityErrorHandler;
import tn.oliveconnect.common.security.SecurityConfig;
import tn.oliveconnect.m4_carte.dto.DelaiCollecteResponse;
import tn.oliveconnect.m4_carte.dto.RemplissageBennesResponse;
import tn.oliveconnect.m4_carte.service.DashboardService;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DashboardController.class)
@Import({SecurityConfig.class, JwtService.class, RestSecurityErrorHandler.class})
class DashboardControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    DashboardService dashboardService;

    @BeforeEach
    void stubs() {
        when(dashboardService.rendement()).thenReturn(List.of());
        when(dashboardService.delais()).thenReturn(new DelaiCollecteResponse(0, null, null, null, List.of(), List.of()));
        when(dashboardService.remplissageBennes()).thenReturn(new RemplissageBennesResponse(0, null, List.of()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/dashboard/rendement", "/api/dashboard/delais", "/api/dashboard/remplissage-bennes"})
    @WithMockUser(roles = "RESPONSABLE_COLLECTE")
    void responsableDeCollecteAcces(String chemin) throws Exception {
        mockMvc.perform(get(chemin)).andExpect(status().isOk());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/dashboard/rendement", "/api/dashboard/delais", "/api/dashboard/remplissage-bennes"})
    @WithMockUser(roles = "ADMINISTRATEUR")
    void administrateurAcces(String chemin) throws Exception {
        mockMvc.perform(get(chemin)).andExpect(status().isOk());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/dashboard/rendement", "/api/dashboard/delais", "/api/dashboard/remplissage-bennes"})
    @WithMockUser(roles = "AGRICULTEUR")
    void agriculteurRefuse(String chemin) throws Exception {
        mockMvc.perform(get(chemin)).andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/dashboard/rendement", "/api/dashboard/delais", "/api/dashboard/remplissage-bennes"})
    void anonymeRefuse(String chemin) throws Exception {
        mockMvc.perform(get(chemin)).andExpect(status().isUnauthorized());
    }
}
