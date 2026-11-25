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
import tn.oliveconnect.m3_ressources.dto.TracteurRequest;
import tn.oliveconnect.m3_ressources.dto.TracteurResponse;
import tn.oliveconnect.m3_ressources.entity.Tracteur;
import tn.oliveconnect.m3_ressources.entity.TypeRessource;
import tn.oliveconnect.m3_ressources.repository.TracteurRepository;

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
class TracteurServiceTest {

    @Mock TracteurRepository repository;
    @Mock RechercheDisponibiliteService rechercheDisponibiliteService;
    @InjectMocks TracteurService service;

    @Test
    void effectueLeCrudEtRetourneUneListePaginee() {
        when(repository.save(any(Tracteur.class))).thenAnswer(invocation -> {
            Tracteur value = invocation.getArgument(0);
            value.setId("t1");
            return value;
        });
        when(repository.findById("t1")).thenAnswer(invocation -> Optional.of(repositoryEntity("t1", "Tracteur A")));
        when(repository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(repositoryEntity("t1", "Tracteur A"))));

        TracteurResponse created = service.creer(new TracteurRequest("Tracteur A", true, "123TU456", 90.0));
        TracteurResponse updated = service.modifier("t1", new TracteurRequest("Tracteur B", false, "789TU456", 110.0));

        assertEquals("t1", created.id());
        assertEquals("Tracteur B", updated.nom());
        assertEquals(110.0, updated.puissance());
        assertEquals("Tracteur A", service.obtenir("t1").nom());
        assertEquals(1, service.lister(0, 20).totalElements());
        service.supprimer("t1");
        verify(repository).deleteById("t1");
    }

    @Test
    void leve404SiTracteurAbsent() {
        when(repository.findById("inconnu")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.obtenir("inconnu"));
    }

    @Test
    void rechercheUneImmatriculationNormaliseeDansLaCollectionTracteurs() {
        when(repository.existsByImmatriculationEquivalent("^ *\\Q123 TU 456\\E *$")).thenReturn(true);

        assertEquals(true, service.existeImmatriculation(" 123 TU 456 "));
        verify(repository).existsByImmatriculationEquivalent("^ *\\Q123 TU 456\\E *$");
    }

    @Test
    void rechercheUniquementLesTracteursDisponibles() {
        LocalDateTime debut = LocalDateTime.parse("2026-03-01T08:00:00");
        LocalDateTime fin = LocalDateTime.parse("2026-03-01T18:00:00");
        when(repository.findByDisponibiliteTrue(any(Sort.class)))
                .thenReturn(List.of(repositoryEntity("t1", "Tracteur A")));
        when(rechercheDisponibiliteService.filtrer(anyList(), eq(TypeRessource.TRACTEUR), eq(debut), eq(fin),
                any(Pageable.class), any()))
                .thenReturn(new PageResponse<>(List.of(new TracteurResponse("t1", "Tracteur A", true, "123TU456", 90.0)), 0, 20, 1));
        var result = service.disponibles(debut, fin, 0, 20);
        assertEquals(1, result.totalElements());
        assertEquals(true, result.content().get(0).disponibilite());
    }

    private Tracteur repositoryEntity(String id, String nom) {
        Tracteur value = new Tracteur();
        value.setId(id);
        value.setNom(nom);
        value.setDisponibilite(true);
        value.setImmatriculation("123TU456");
        value.setPuissance(90.0);
        return value;
    }
}
