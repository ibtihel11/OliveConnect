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
import tn.oliveconnect.m3_ressources.dto.OuvrierRequest;
import tn.oliveconnect.m3_ressources.dto.OuvrierResponse;
import tn.oliveconnect.m3_ressources.entity.Ouvrier;
import tn.oliveconnect.m3_ressources.entity.TypeRessource;
import tn.oliveconnect.m3_ressources.repository.OuvrierRepository;

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
class OuvrierServiceTest {

    @Mock OuvrierRepository repository;
    @Mock RechercheDisponibiliteService rechercheDisponibiliteService;
    @InjectMocks OuvrierService service;

    @Test
    void effectueLeCrudEtRetourneUneListePaginee() {
        when(repository.save(any(Ouvrier.class))).thenAnswer(invocation -> {
            Ouvrier value = invocation.getArgument(0);
            value.setId("o1");
            return value;
        });
        when(repository.findById("o1")).thenAnswer(invocation -> Optional.of(repositoryEntity("o1", "Olivier")));
        when(repository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(repositoryEntity("o1", "Olivier"))));

        OuvrierResponse created = service.creer(new OuvrierRequest("Olivier", true, "Récolte", true));
        OuvrierResponse updated = service.modifier("o1", new OuvrierRequest("Olivier A.", false, "Taille", false));

        assertEquals("o1", created.id());
        assertEquals("Olivier A.", updated.nom());
        assertEquals("Taille", updated.specialite());
        assertEquals("Olivier", service.obtenir("o1").nom());
        assertEquals(1, service.lister(0, 20).totalElements());
        service.supprimer("o1");
        verify(repository).deleteById("o1");
    }

    @Test
    void leve404SiOuvrierAbsent() {
        when(repository.findById("inconnu")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.obtenir("inconnu"));
    }

    @Test
    void rechercheUniquementLesOuvriersDisponiblesEtValideLaPeriode() {
        LocalDateTime debut = LocalDateTime.parse("2026-03-01T08:00:00");
        LocalDateTime fin = LocalDateTime.parse("2026-03-01T18:00:00");
        when(repository.findByDisponibiliteTrue(any(Sort.class)))
                .thenReturn(List.of(repositoryEntity("o1", "Olivier")));
        when(rechercheDisponibiliteService.filtrer(anyList(), eq(TypeRessource.OUVRIER), eq(debut), eq(fin),
                any(Pageable.class), any()))
                .thenReturn(new PageResponse<>(List.of(new OuvrierResponse("o1", "Olivier", true, "Récolte", true)), 0, 20, 1));

        var result = service.disponibles(debut, fin, 0, 20);

        assertEquals(1, result.totalElements());
        assertEquals(true, result.content().get(0).disponibilite());
        assertThrows(tn.oliveconnect.common.exception.BadRequestException.class,
                () -> service.disponibles(fin, debut, 0, 20));
    }

    private Ouvrier repositoryEntity(String id, String nom) {
        Ouvrier value = new Ouvrier();
        value.setId(id);
        value.setNom(nom);
        value.setDisponibilite(true);
        value.setSpecialite("Récolte");
        value.setSaisonnier(true);
        return value;
    }
}
