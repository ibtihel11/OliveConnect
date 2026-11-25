package tn.oliveconnect.m3_ressources.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import tn.oliveconnect.common.dto.PageResponse;
import tn.oliveconnect.common.exception.NotFoundException;
import tn.oliveconnect.m3_ressources.dto.BenneRequest;
import tn.oliveconnect.m3_ressources.dto.BenneResponse;
import tn.oliveconnect.m3_ressources.entity.Benne;
import tn.oliveconnect.m3_ressources.entity.TypeRessource;
import tn.oliveconnect.m3_ressources.repository.BenneRepository;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BenneServiceTest {

    @Mock BenneRepository repository;
    @Mock RechercheDisponibiliteService rechercheDisponibiliteService;
    @InjectMocks BenneService service;

    @Test
    void effectueLeCrudEtRetourneUneListePaginee() {
        when(repository.save(any(Benne.class))).thenAnswer(invocation -> {
            Benne value = invocation.getArgument(0);
            value.setId("b1");
            return value;
        });
        when(repository.findById("b1")).thenAnswer(invocation -> Optional.of(repositoryEntity("b1", "Benne A")));
        when(repository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(repositoryEntity("b1", "Benne A"))));

        BenneResponse created = service.creer(new BenneRequest("Benne A", true, 1500.0, "123TU456"));
        BenneResponse updated = service.modifier("b1", new BenneRequest("Benne B", false, 2000.0, "789TU456"));

        assertEquals("b1", created.id());
        assertEquals("Benne B", updated.nom());
        assertEquals(2000.0, updated.capacite());
        assertEquals("123TU456", service.obtenir("b1").immatriculation());
        assertEquals(1, service.lister(0, 20).totalElements());
        service.supprimer("b1");
        verify(repository).deleteById("b1");
    }

    @Test
    void leve404SiBenneAbsente() {
        when(repository.findById("inconnu")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.obtenir("inconnu"));
    }

    @Test
    void rechercheUneImmatriculationNormaliseeDansLaCollectionBennes() {
        when(repository.existsByImmatriculationEquivalent("^ *\\Q123 TU 456\\E *$")).thenReturn(true);

        assertEquals(true, service.existeImmatriculation(" 123 TU 456 "));
        verify(repository).existsByImmatriculationEquivalent("^ *\\Q123 TU 456\\E *$");
    }

    @Test
    void rechercheUniquementLesBennesDisponibles() {
        LocalDateTime debut = LocalDateTime.parse("2026-03-01T08:00:00");
        LocalDateTime fin = LocalDateTime.parse("2026-03-01T18:00:00");
        when(repository.findByDisponibiliteTrue(any(Sort.class)))
                .thenReturn(List.of(repositoryEntity("b1", "Benne A")));
        when(rechercheDisponibiliteService.filtrer(anyList(), eq(TypeRessource.BENNE), eq(debut), eq(fin),
                any(Pageable.class), any()))
                .thenReturn(new PageResponse<>(List.of(new BenneResponse("b1", "Benne A", true, 1500.0, "123TU456")), 0, 20, 1));
        var result = service.disponibles(debut, fin, 0, 20);
        assertEquals(1, result.totalElements());
        assertEquals(true, result.content().get(0).disponibilite());
    }

    private Benne repositoryEntity(String id, String nom) {
        Benne value = new Benne();
        value.setId(id);
        value.setNom(nom);
        value.setDisponibilite(true);
        value.setCapacite(1500.0);
        value.setImmatriculation("123TU456");
        return value;
    }
}
