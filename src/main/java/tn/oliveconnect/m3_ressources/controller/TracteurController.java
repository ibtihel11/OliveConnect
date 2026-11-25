package tn.oliveconnect.m3_ressources.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tn.oliveconnect.common.dto.PageResponse;
import tn.oliveconnect.m3_ressources.dto.TracteurRequest;
import tn.oliveconnect.m3_ressources.dto.TracteurResponse;
import tn.oliveconnect.m3_ressources.service.TracteurService;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/ressources/tracteurs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('RESPONSABLE_LOGISTIQUE')")
public class TracteurController {

    private final TracteurService service;

    @GetMapping
    public PageResponse<TracteurResponse> lister(@RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "20") int size) {
        return service.lister(page, size);
    }

    @GetMapping("/disponibles")
    @PreAuthorize("hasAnyRole('RESPONSABLE_LOGISTIQUE', 'RESPONSABLE_COLLECTE')")
    public PageResponse<TracteurResponse> disponibles(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime debut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return service.disponibles(debut, fin, page, size);
    }

    @GetMapping("/{id}")
    public TracteurResponse obtenir(@PathVariable String id) {
        return service.obtenir(id);
    }

    @PostMapping
    public ResponseEntity<TracteurResponse> creer(@Valid @RequestBody TracteurRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.creer(request));
    }

    @PutMapping("/{id}")
    public TracteurResponse modifier(@PathVariable String id, @Valid @RequestBody TracteurRequest request) {
        return service.modifier(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable String id) {
        service.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}
