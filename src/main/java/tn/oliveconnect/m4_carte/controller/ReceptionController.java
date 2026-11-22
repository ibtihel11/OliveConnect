package tn.oliveconnect.m4_carte.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tn.oliveconnect.common.dto.PageResponse;
import tn.oliveconnect.m4_carte.dto.ReceptionRequest;
import tn.oliveconnect.m4_carte.dto.ReceptionResponse;
import tn.oliveconnect.m4_carte.service.ReceptionService;

@RestController
@RequestMapping("/api/receptions")
@RequiredArgsConstructor
public class ReceptionController {

    private final ReceptionService receptionService;

    @PostMapping
    @PreAuthorize("hasRole('RESPONSABLE_PRESSOIR')")
    public ResponseEntity<ReceptionResponse> enregistrer(@Valid @RequestBody ReceptionRequest request,
                                                         Authentication authentication) {
        ReceptionResponse created = receptionService.enregistrer(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public PageResponse<ReceptionResponse> lister(@RequestParam(required = false) String tourneeId,
                                                  @RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "20") int size) {
        return receptionService.lister(tourneeId, page, size);
    }
}
