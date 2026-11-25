package tn.oliveconnect.m3_ressources.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import tn.oliveconnect.m3_ressources.dto.ImportRessourcesResponse;
import tn.oliveconnect.m3_ressources.service.ImportRessourcesService;

@RestController
@RequestMapping("/api/ressources/import")
@RequiredArgsConstructor
@PreAuthorize("hasRole('RESPONSABLE_LOGISTIQUE')")
public class ImportRessourcesController {

    private final ImportRessourcesService service;

    @PostMapping
    public ImportRessourcesResponse importer(@RequestParam("file") MultipartFile fichier) {
        return service.importer(fichier);
    }
}
