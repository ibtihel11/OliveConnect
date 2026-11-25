package tn.oliveconnect.m3_ressources.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.PageRequest;
import tn.oliveconnect.common.dto.PageResponse;
import tn.oliveconnect.common.exception.NotFoundException;
import tn.oliveconnect.m3_ressources.dto.MaterielRequest;
import tn.oliveconnect.m3_ressources.dto.MaterielResponse;
import tn.oliveconnect.m3_ressources.entity.Materiel;
import tn.oliveconnect.m3_ressources.entity.TypeRessource;
import tn.oliveconnect.m3_ressources.repository.MaterielRepository;

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
class MaterielServiceTest {

    @Mock MaterielRepository repository;
    @Mock RechercheDisponibiliteService rechercheDisponibiliteService;
    @InjectMocks MaterielService service;

    @Test
    void effectueLeCrudEtRetourneUneListePaginee() {
        when(repository.save(any(Materiel.class))).thenAnswer(invocation -> {
            Materiel value = invocation.getArgument(0);
            value.setId("m1");
            return value;
        });
        when(repository.findById("m1")).thenAnswer(invocation -> Optional.of(repositoryEntity("m1", "Filets")));
        when(repository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(repositoryEntity("m1", "Filets"))));

        MaterielResponse created = service.creer(new MaterielRequest("Filets", true, "FILETS", 8, 3));
        MaterielResponse updated = service.modifier("m1", new MaterielRequest("Filets", true, "FILETS", 2, 4));

        assertEquals("m1", created.id());
        assertEquals(2, updated.quantiteStock());
        assertEquals(4, updated.seuilMinimum());
        assertEquals("Filets", service.obtenir("m1").nom());
        assertEquals(1, service.lister(0, 20).totalElements());
        service.supprimer("m1");
        verify(repository).deleteById("m1");
    }

    @Test
    void leve404SiMaterielAbsent() {
        when(repository.findById("inconnu")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.obtenir("inconnu"));
    }

    @Test
    void detecteLeStockInferieurAuSeuil() {
        when(repository.findAll(any(org.springframework.data.domain.Sort.class)))
                .thenReturn(List.of(materiel("m1", "Filets", "filet", 2, 5)));

        List<MaterielResponse> result = service.listerStocksBas();

        assertEquals(1, result.size());
        assertEquals("m1", result.get(0).id());
        assertEquals("Filets", result.get(0).nom());
        assertEquals("filet", result.get(0).type());
        assertEquals(2, result.get(0).quantiteStock());
        assertEquals(5, result.get(0).seuilMinimum());
    }

    @Test
    void detecteLeStockEgalAuSeuil() {
        when(repository.findAll(any(org.springframework.data.domain.Sort.class)))
                .thenReturn(List.of(materiel("m2", "Vibrateurs", "vibrateur", 4, 4)));

        assertEquals(List.of("m2"), service.listerStocksBas().stream().map(MaterielResponse::id).toList());
    }

    @Test
    void exclutLeStockSuperieurAuSeuil() {
        when(repository.findAll(any(org.springframework.data.domain.Sort.class)))
                .thenReturn(List.of(materiel("m3", "Autre matériel", "autre", 6, 4)));

        assertEquals(List.of(), service.listerStocksBas());
    }

    @Test
    void retourneUneListeVideSansMaterielEnStockBas() {
        when(repository.findAll(any(org.springframework.data.domain.Sort.class)))
                .thenReturn(List.of(materiel("m1", "Matériel A", "filet", 6, 4),
                        materiel("m2", "Matériel B", "vibrateur", 5, 2)));

        assertEquals(List.of(), service.listerStocksBas());
    }

    @Test
    void rechercheUniquementLesMaterielsDisponibles() {
        LocalDateTime debut = LocalDateTime.parse("2026-03-01T08:00:00");
        LocalDateTime fin = LocalDateTime.parse("2026-03-01T18:00:00");
        when(repository.findByDisponibiliteTrue(any(Sort.class)))
                .thenReturn(List.of(materiel("m1", "Filets", "filet", 8, 3)));
        when(rechercheDisponibiliteService.filtrer(anyList(), eq(TypeRessource.MATERIEL), eq(debut), eq(fin),
                any(Pageable.class), any()))
                .thenReturn(new PageResponse<>(List.of(new MaterielResponse("m1", "Filets", true, "filet", 8, 3)), 0, 20, 1));
        var result = service.disponibles(debut, fin, 0, 20);
        assertEquals(1, result.totalElements());
        assertEquals(true, result.content().get(0).disponibilite());
    }

    private Materiel repositoryEntity(String id, String nom) {
        return materiel(id, nom, "FILETS", 8, 3);
    }

    private Materiel materiel(String id, String nom, String type, int quantiteStock, int seuilMinimum) {
        Materiel value = new Materiel();
        value.setId(id);
        value.setNom(nom);
        value.setDisponibilite(true);
        value.setType(type);
        value.setQuantiteStock(quantiteStock);
        value.setSeuilMinimum(seuilMinimum);
        return value;
    }
}
