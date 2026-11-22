package tn.oliveconnect.common.security;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private static final String SECRET = "test-secret-test-secret-test-secret-0123456789";

    @Test
    void tokenGenereEstRelu() {
        JwtService service = new JwtService(SECRET, 5);
        String token = service.generate("u-pressoir", "RESPONSABLE_PRESSOIR");

        Optional<AuthenticatedUser> user = service.parse(token);

        assertTrue(user.isPresent());
        assertEquals("u-pressoir", user.get().id());
        assertEquals("RESPONSABLE_PRESSOIR", user.get().role());
    }

    @Test
    void tokenSigneAvecUneAutreCleEstRefuse() {
        String token = new JwtService("autre-secret-autre-secret-autre-secret-01234", 5).generate("u1", "AGRICULTEUR");

        assertTrue(new JwtService(SECRET, 5).parse(token).isEmpty());
    }

    @Test
    void tokenFalsifieOuIllisibleEstRefuse() {
        JwtService service = new JwtService(SECRET, 5);
        String token = service.generate("u1", "AGRICULTEUR");

        assertTrue(service.parse(token + "x").isEmpty());
        assertTrue(service.parse("n'importe quoi").isEmpty());
        assertTrue(service.parse("").isEmpty());
    }

    @Test
    void tokenExpireEstRefuse() {
        JwtService service = new JwtService(SECRET, -1);

        assertTrue(service.parse(service.generate("u1", "AGRICULTEUR")).isEmpty());
    }

    @Test
    void secretTropCourtEstRefuse() {
        assertThrows(IllegalStateException.class, () -> new JwtService("trop-court", 5));
    }
}
