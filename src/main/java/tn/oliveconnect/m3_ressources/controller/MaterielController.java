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
import tn.oliveconnect.m3_ressources.dto.MaterielRequest;
import tn.oliveconnect.m3_ressources.dto.MaterielResponse;
import tn.oliveconnect.m3_ressources.service.MaterielService;

import java.util.List;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/ressources/materiels")
@RequiredArgsConstructor
@PreAuthorize("hasRole('RESPONSABLE_LOGISTIQUE')")
public class MaterielController {

    private final MaterielService service;

    @GetMapping
    public PageResponse<MaterielResponse> lister(@RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "20") int size) {
        return service.lister(page, size);
    }

    @GetMapping("/stock-bas")
    public List<MaterielResponse> listerStocksBas() {
        return service.listerStocksBas();
    }

    @GetMapping("/disponibles")
    @PreAuthorize("hasAnyRole('RESPONSABLE_LOGISTIQUE', 'RESPONSABLE_COLLECTE')")
    public PageResponse<MaterielResponse> disponibles(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime debut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return service.disponibles(debut, fin, page, size);
    }

    @GetMapping("/{id}")
    public MaterielResponse obtenir(@PathVariable String id) {
        return service.obtenir(id);
    }

    @PostMapping
    public ResponseEntity<MaterielResponse> creer(@Valid @RequestBody MaterielRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.creer(request));
    }

    @PutMapping("/{id}")
    public MaterielResponse modifier(@PathVariable String id, @Valid @RequestBody MaterielRequest request) {
        return service.modifier(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> supprimer(@PathVariable String id) {
        service.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}
