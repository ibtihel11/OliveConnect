package tn.oliveconnect.m4_carte.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import tn.oliveconnect.common.dto.PageResponse;
import tn.oliveconnect.common.dto.Paging;
import tn.oliveconnect.m4_carte.dto.PressoirRequest;
import tn.oliveconnect.m4_carte.dto.PressoirResponse;
import tn.oliveconnect.m4_carte.entity.Pressoir;
import tn.oliveconnect.m4_carte.repository.PressoirRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PressoirService {

    private final PressoirRepository pressoirRepository;

    /** Résultat de POST /api/pressoirs : created = true -> 201, false -> 200 (mise à jour). */
    public record Declaration(PressoirResponse pressoir, boolean created) {
    }

    /**
     * Le responsable du pressoir communique son pressoir et sa capacité de traitement.
     * Relation 1 - 1 (cadrage section 3) : s'il gère déjà un pressoir, celui-ci est mis à jour.
     */
    public Declaration declarer(PressoirRequest request, String responsableId) {
        Optional<Pressoir> existant = pressoirRepository.findByResponsableId(responsableId);
        boolean created = existant.isEmpty();
        Pressoir pressoir = existant.orElseGet(() -> Pressoir.builder().responsableId(responsableId).build());
        pressoir.setNom(request.nom());
        pressoir.setAdresse(request.adresse());
        pressoir.setCapaciteTraitement(request.capaciteTraitement());
        return new Declaration(PressoirResponse.from(pressoirRepository.save(pressoir)), created);
    }

    /** Les pressoirs gérés par l'utilisateur connecté. */
    public PageResponse<PressoirResponse> lister(String responsableId, int page, int size) {
        Pageable pageable = Paging.of(page, size, Sort.by("nom"));
        Page<PressoirResponse> result = pressoirRepository.findByResponsableId(responsableId, pageable)
                .map(PressoirResponse::from);
        return PageResponse.from(result);
    }
}
