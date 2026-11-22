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
import tn.oliveconnect.common.security.JwtService;
import tn.oliveconnect.common.security.RestSecurityErrorHandler;
import tn.oliveconnect.common.security.SecurityConfig;
import tn.oliveconnect.m4_carte.dto.PressoirRequest;
import tn.oliveconnect.m4_carte.dto.PressoirResponse;
import tn.oliveconnect.m4_carte.service.PressoirService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PressoirController.class)
@Import({SecurityConfig.class, JwtService.class, RestSecurityErrorHandler.class})
class PressoirControllerTest {

    private static final String CORPS = "{\"nom\":\"Pressoir Moderne\",\"adresse\":\"Sfax\",\"capaciteTraitement\":20000}";

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    PressoirService pressoirService;

    private static PressoirResponse reponse() {
        return new PressoirResponse("p1", "Pressoir Moderne", "Sfax", 20000.0, "u-pressoir");
    }

    @Test
    @WithMockUser(username = "u-pressoir", roles = "RESPONSABLE_PRESSOIR")
    void premiereDeclarationRenvoie201() throws Exception {
        when(pressoirService.declarer(any(PressoirRequest.class), eq("u-pressoir")))
                .thenReturn(new PressoirService.Declaration(reponse(), true));

        mockMvc.perform(post("/api/pressoirs").contentType(MediaType.APPLICATION_JSON).content(CORPS))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("p1"))
                .andExpect(jsonPath("$.capaciteTraitement").value(20000.0));
    }

    @Test
    @WithMockUser(username = "u-pressoir", roles = "RESPONSABLE_PRESSOIR")
    void miseAJourRenvoie200() throws Exception {
        when(pressoirService.declarer(any(PressoirRequest.class), eq("u-pressoir")))
                .thenReturn(new PressoirService.Declaration(reponse(), false));

        mockMvc.perform(post("/api/pressoirs").contentType(MediaType.APPLICATION_JSON).content(CORPS))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "u-pressoir", roles = "RESPONSABLE_PRESSOIR")
    void capaciteNegativeRenvoie400() throws Exception {
        mockMvc.perform(post("/api/pressoirs").contentType(MediaType.APPLICATION_JSON)
                        .content(CORPS.replace("20000", "-1")))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "u-pressoir", roles = "RESPONSABLE_PRESSOIR")
    void listeRenvoieLaPage() throws Exception {
        when(pressoirService.lister("u-pressoir", 0, 20)).thenReturn(new PageResponse<>(List.of(reponse()), 0, 20, 1));

        mockMvc.perform(get("/api/pressoirs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nom").value("Pressoir Moderne"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser(username = "u-collecte", roles = "RESPONSABLE_COLLECTE")
    void autreRoleRenvoie403() throws Exception {
        mockMvc.perform(get("/api/pressoirs")).andExpect(status().isForbidden());
        mockMvc.perform(post("/api/pressoirs").contentType(MediaType.APPLICATION_JSON).content(CORPS))
                .andExpect(status().isForbidden());
    }

    @Test
    void sansAuthentificationRenvoie401() throws Exception {
        mockMvc.perform(get("/api/pressoirs")).andExpect(status().isUnauthorized());
    }
}
