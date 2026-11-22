package tn.oliveconnect.common.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tn.oliveconnect.common.exception.BadRequestException;

import java.util.Map;

/**
 * UNIQUEMENT profil "dev" : génère un JWT pour tester M4 sans attendre le login de M1.
 * A supprimer (ou laisser inactif) une fois /api/auth/login livré par M1.
 */
@Profile("dev")
@RestController
@RequestMapping("/api/dev")
@RequiredArgsConstructor
public class DevTokenController {

    private final JwtService jwtService;

    @GetMapping("/token")
    public Map<String, String> token(@RequestParam String userId, @RequestParam String role) {
        if (!Roles.TOUS.contains(role)) {
            throw new BadRequestException("Rôle inconnu : " + role + " (valeurs : " + String.join(", ", Roles.TOUS) + ")");
        }
        return Map.of("token", jwtService.generate(userId, role), "userId", userId, "role", role);
    }
}
