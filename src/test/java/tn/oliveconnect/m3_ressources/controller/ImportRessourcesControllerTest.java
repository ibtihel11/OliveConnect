package tn.oliveconnect.m3_ressources.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tn.oliveconnect.common.security.JwtService;
import tn.oliveconnect.common.security.RestSecurityErrorHandler;
import tn.oliveconnect.common.security.SecurityConfig;
import tn.oliveconnect.m3_ressources.dto.ImportErreurResponse;
import tn.oliveconnect.m3_ressources.dto.ImportRessourcesResponse;
import tn.oliveconnect.m3_ressources.service.ImportRessourcesService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ImportRessourcesController.class)
@Import({SecurityConfig.class, JwtService.class, RestSecurityErrorHandler.class})
class ImportRessourcesControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean ImportRessourcesService service;

    @Test
    @WithMockUser(username = "logistique", roles = "RESPONSABLE_LOGISTIQUE")
    void importeMultipartEtRetourneLeRapport() throws Exception {
        when(service.importer(any())).thenReturn(new ImportRessourcesResponse(2, 1, 1,
                List.of(new ImportErreurResponse(3, "nom est obligatoire"))));
        MockMultipartFile fichier = new MockMultipartFile("file", "ouvriers.json", "application/json",
                "[]".getBytes(java.nio.charset.StandardCharsets.UTF_8));

        mockMvc.perform(multipart("/api/ressources/import").file(fichier))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalLignes").value(2))
                .andExpect(jsonPath("$.nombreImportes").value(1))
                .andExpect(jsonPath("$.nombreRejetes").value(1))
                .andExpect(jsonPath("$.erreurs[0].ligne").value(3))
                .andExpect(jsonPath("$.erreurs[0].motif").value("nom est obligatoire"));
    }

    @Test
    @WithMockUser(username = "collecte", roles = "RESPONSABLE_COLLECTE")
    void reserveImportAuResponsableLogistique() throws Exception {
        mockMvc.perform(multipart("/api/ressources/import").file(new MockMultipartFile(
                        "file", "ouvriers.json", "application/json", "[]".getBytes())))
                .andExpect(status().isForbidden());
    }

    @Test
    void exigeUneAuthentification() throws Exception {
        mockMvc.perform(multipart("/api/ressources/import").file(new MockMultipartFile(
                        "file", "ouvriers.json", "application/json", "[]".getBytes())))
                .andExpect(status().isUnauthorized());
    }
}
