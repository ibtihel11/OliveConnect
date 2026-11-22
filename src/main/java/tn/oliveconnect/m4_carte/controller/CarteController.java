package tn.oliveconnect.m4_carte.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.oliveconnect.m4_carte.dto.CartePubliqueVergerResponse;
import tn.oliveconnect.m4_carte.dto.CarteVergerResponse;
import tn.oliveconnect.m4_carte.service.CarteService;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CarteController {

    private final CarteService carteService;

    /** Public : vergers avec position, maturité, type d'olives. Aucune authentification. */
    @GetMapping("/public/carte")
    @PreAuthorize("permitAll()")
    public List<CartePubliqueVergerResponse> cartePublique() {
        return carteService.carteVergersPublique();
    }

    /** Connecté : version complète (statut de récolte, relevés, météo...). */
    @GetMapping("/carte/vergers")
    @PreAuthorize("isAuthenticated()")
    public List<CarteVergerResponse> carteComplete() {
        return carteService.carteVergersComplete();
    }
}
