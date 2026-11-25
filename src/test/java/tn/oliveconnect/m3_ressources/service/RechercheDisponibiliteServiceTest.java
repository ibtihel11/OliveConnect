package tn.oliveconnect.m3_ressources.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import tn.oliveconnect.m3_ressources.dto.OuvrierResponse;
import tn.oliveconnect.m3_ressources.entity.Ouvrier;
import tn.oliveconnect.m3_ressources.entity.TypeRessource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RechercheDisponibiliteServiceTest {

    @Mock AffectationService affectationService;
    @InjectMocks RechercheDisponibiliteService service;

    private final LocalDateTime debut = LocalDateTime.parse("2026-11-25T10:00:00");
    private final LocalDateTime fin = LocalDateTime.parse("2026-11-25T12:00:00");

    @Test
    void ressourceSansAffectationEstRetournee() {
        when(affectationService.ressourcesEnConflit(TypeRessource.OUVRIER, debut, fin)).thenReturn(Set.of());

        var result = rechercher(List.of(ouvrier("libre", true)));

        assertEquals(List.of("libre"), ids(result.content()));
    }

    @Test
    void affectationCouvrantTouteLaPeriodeExclutLaRessource() {
        when(affectationService.ressourcesEnConflit(TypeRessource.OUVRIER, debut, fin)).thenReturn(Set.of("complet"));

        var result = rechercher(List.of(ouvrier("complet", true)));

        assertEquals(List.of(), ids(result.content()));
    }

    @Test
    void affectationPartielleExclutLaRessource() {
        when(affectationService.ressourcesEnConflit(TypeRessource.OUVRIER, debut, fin)).thenReturn(Set.of("partiel"));

        var result = rechercher(List.of(ouvrier("partiel", true)));

        assertEquals(List.of(), ids(result.content()));
    }

    @Test
    void affectationTermineeExactementAuDebutNeBloquePas() {
        when(affectationService.ressourcesEnConflit(TypeRessource.OUVRIER, debut, fin)).thenReturn(Set.of());

        var result = rechercher(List.of(ouvrier("adjacent-debut", true)));

        assertEquals(List.of("adjacent-debut"), ids(result.content()));
    }

    @Test
    void affectationCommencantExactementALaFinNeBloquePas() {
        when(affectationService.ressourcesEnConflit(TypeRessource.OUVRIER, debut, fin)).thenReturn(Set.of());

        var result = rechercher(List.of(ouvrier("adjacent-fin", true)));

        assertEquals(List.of("adjacent-fin"), ids(result.content()));
    }

    @Test
    void affectationDuMemeIdentifiantMaisDUnAutreTypeNeBloquePas() {
        // La recherche OUVRIER demande uniquement les conflits OUVRIER; un conflit BENNE du même ID n'est pas inclus.
        when(affectationService.ressourcesEnConflit(TypeRessource.OUVRIER, debut, fin)).thenReturn(Set.of());

        var result = rechercher(List.of(ouvrier("partage", true)));

        assertEquals(List.of("partage"), ids(result.content()));
        verify(affectationService).ressourcesEnConflit(TypeRessource.OUVRIER, debut, fin);
    }

    @Test
    void ressourceIndisponibleResteExclue() {
        when(affectationService.ressourcesEnConflit(TypeRessource.OUVRIER, debut, fin)).thenReturn(Set.of());

        var result = rechercher(List.of(ouvrier("indisponible", false)));

        assertEquals(List.of(), ids(result.content()));
    }

    private tn.oliveconnect.common.dto.PageResponse<OuvrierResponse> rechercher(List<Ouvrier> ressources) {
        return service.filtrer(ressources, TypeRessource.OUVRIER, debut, fin,
                PageRequest.of(0, 20, Sort.by("nom")), OuvrierResponse::from);
    }

    private Ouvrier ouvrier(String id, boolean disponible) {
        Ouvrier value = new Ouvrier();
        value.setId(id);
        value.setNom(id);
        value.setDisponibilite(disponible);
        value.setSpecialite("récolte");
        value.setSaisonnier(false);
        return value;
    }

    private List<String> ids(List<OuvrierResponse> responses) {
        return responses.stream().map(OuvrierResponse::id).toList();
    }
}
