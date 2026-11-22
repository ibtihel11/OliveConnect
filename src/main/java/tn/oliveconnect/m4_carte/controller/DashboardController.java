package tn.oliveconnect.m4_carte.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.oliveconnect.m4_carte.dto.DelaiCollecteResponse;
import tn.oliveconnect.m4_carte.dto.RemplissageBennesResponse;
import tn.oliveconnect.m4_carte.dto.RendementVergerResponse;
import tn.oliveconnect.m4_carte.service.DashboardService;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/rendement")
    @PreAuthorize("hasAnyRole('RESPONSABLE_COLLECTE', 'ADMINISTRATEUR')")
    public List<RendementVergerResponse> rendement() {
        return dashboardService.rendement();
    }

    @GetMapping("/delais")
    @PreAuthorize("hasAnyRole('RESPONSABLE_COLLECTE', 'ADMINISTRATEUR')")
    public DelaiCollecteResponse delais() {
        return dashboardService.delais();
    }

    @GetMapping("/remplissage-bennes")
    @PreAuthorize("hasAnyRole('RESPONSABLE_COLLECTE', 'ADMINISTRATEUR')")
    public RemplissageBennesResponse remplissageBennes() {
        return dashboardService.remplissageBennes();
    }
}
