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
import tn.oliveconnect.m4_carte.dto.PressoirRequest;
import tn.oliveconnect.m4_carte.dto.PressoirResponse;
import tn.oliveconnect.m4_carte.service.PressoirService;

@RestController
@RequestMapping("/api/pressoirs")
@RequiredArgsConstructor
public class PressoirController {

    private final PressoirService pressoirService;

    @GetMapping
    @PreAuthorize("hasRole('RESPONSABLE_PRESSOIR')")
    public PageResponse<PressoirResponse> lister(@RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "20") int size,
                                                 Authentication authentication) {
        return pressoirService.lister(authentication.getName(), page, size);
    }

    /** 201 à la première déclaration, 200 si le pressoir du responsable est mis à jour (relation 1 - 1). */
    @PostMapping
    @PreAuthorize("hasRole('RESPONSABLE_PRESSOIR')")
    public ResponseEntity<PressoirResponse> declarer(@Valid @RequestBody PressoirRequest request,
                                                     Authentication authentication) {
        PressoirService.Declaration d = pressoirService.declarer(request, authentication.getName());
        return ResponseEntity.status(d.created() ? HttpStatus.CREATED : HttpStatus.OK).body(d.pressoir());
    }
}
