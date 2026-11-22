package tn.oliveconnect.m4_carte.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import tn.oliveconnect.common.security.JwtService;
import tn.oliveconnect.common.security.RestSecurityErrorHandler;
import tn.oliveconnect.common.security.SecurityConfig;
import tn.oliveconnect.m4_carte.dto.CartePubliqueVergerResponse;
import tn.oliveconnect.m4_carte.dto.CarteVergerResponse;
import tn.oliveconnect.m4_carte.service.CarteService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CarteController.class)
@Import({SecurityConfig.class, JwtService.class, RestSecurityErrorHandler.class})
class CarteControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    CarteService carteService;

    @Test
    void cartePubliqueAccessibleSansAuthentificationEtSansDonneesSensibles() throws Exception {
        when(carteService.carteVergersPublique()).thenReturn(List.of(
                new CartePubliqueVergerResponse("v1", "Verger A", 34.74, 10.76, 55.0, "Chemlali")));

        mockMvc.perform(get("/api/public/carte"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("v1"))
                .andExpect(jsonPath("$[0].latitude").value(34.74))
                .andExpect(jsonPath("$[0].longitude").value(10.76))
                .andExpect(jsonPath("$[0].niveauMaturite").value(55.0))
                .andExpect(jsonPath("$[0].typeOlives").value("Chemlali"))
                .andExpect(jsonPath("$[0].statutRecolte").doesNotExist())
                .andExpect(jsonPath("$[0].agriculteurId").doesNotExist());
    }

    @Test
    void carteCompleteExigeUneAuthentification() throws Exception {
        mockMvc.perform(get("/api/carte/vergers")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "AGRICULTEUR")
    void carteCompleteAccessibleAuxProfilsConnectes() throws Exception {
        when(carteService.carteVergersComplete()).thenReturn(List.of(
                new CarteVergerResponse("v1", "Verger A", "Sfax", 12.5, 34.74, 10.76, "EN_RECOLTE", "EN_COURS",
                        "Chemlali", 55.0, LocalDateTime.of(2026, 11, 17, 9, 0), List.of(), null)));

        mockMvc.perform(get("/api/carte/vergers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].statutRecolte").value("EN_COURS"))
                .andExpect(jsonPath("$[0].statutVerger").value("EN_RECOLTE"));
    }
}
