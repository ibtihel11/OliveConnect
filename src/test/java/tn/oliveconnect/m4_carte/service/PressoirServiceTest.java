package tn.oliveconnect.m4_carte.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import tn.oliveconnect.common.dto.PageResponse;
import tn.oliveconnect.m4_carte.dto.PressoirRequest;
import tn.oliveconnect.m4_carte.dto.PressoirResponse;
import tn.oliveconnect.m4_carte.entity.Pressoir;
import tn.oliveconnect.m4_carte.repository.PressoirRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PressoirServiceTest {

    @Mock
    PressoirRepository pressoirRepository;

    @InjectMocks
    PressoirService service;

    private static PressoirRequest requete(double capacite) {
        return new PressoirRequest("Pressoir Moderne", "Sfax", capacite);
    }

    @Test
    void premiereDeclarationCreeLePressoir() {
        when(pressoirRepository.findByResponsableId("u-pressoir")).thenReturn(Optional.empty());
        when(pressoirRepository.save(any(Pressoir.class))).thenAnswer(inv -> {
            Pressoir p = inv.getArgument(0);
            p.setId("p1");
            return p;
        });

        PressoirService.Declaration d = service.declarer(requete(20000), "u-pressoir");

        assertTrue(d.created());
        assertEquals("p1", d.pressoir().id());
        assertEquals("u-pressoir", d.pressoir().responsableId());
        assertEquals(20000.0, d.pressoir().capaciteTraitement());
    }

    @Test
    void secondeDeclarationMetAJourLePressoirExistant() {
        Pressoir existant = Pressoir.builder().id("p1").nom("Ancien").adresse("Ancienne").capaciteTraitement(10000.0)
                .responsableId("u-pressoir").build();
        when(pressoirRepository.findByResponsableId("u-pressoir")).thenReturn(Optional.of(existant));
        when(pressoirRepository.save(any(Pressoir.class))).thenAnswer(inv -> inv.getArgument(0));

        PressoirService.Declaration d = service.declarer(requete(25000), "u-pressoir");

        assertFalse(d.created());
        assertEquals("p1", d.pressoir().id());
        assertEquals("Pressoir Moderne", d.pressoir().nom());
        assertEquals(25000.0, d.pressoir().capaciteTraitement());
    }

    @Test
    void listeLesPressoirsDuResponsable() {
        Pressoir p = Pressoir.builder().id("p1").nom("Pressoir").responsableId("u-pressoir").capaciteTraitement(1.0).build();
        when(pressoirRepository.findByResponsableId(any(String.class), any(Pageable.class)))
                .thenAnswer(inv -> new PageImpl<>(List.of(p), inv.getArgument(1), 1));

        PageResponse<PressoirResponse> page = service.lister("u-pressoir", 0, 20);

        assertEquals(1, page.totalElements());
        assertEquals("p1", page.content().get(0).id());
    }
}
