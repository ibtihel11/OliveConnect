package tn.oliveconnect.m3_ressources.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tn.oliveconnect.m3_ressources.dto.CalendrierRessourceResponse;
import tn.oliveconnect.m3_ressources.service.CalendrierService;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class CalendrierController {

    private final CalendrierService service;

    @GetMapping("/api/calendrier")
    public CalendrierRessourceResponse consulter(
            @RequestParam String ressourceId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime debut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        return service.consulter(ressourceId, debut, fin);
    }
}
