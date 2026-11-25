package tn.oliveconnect.m3_ressources.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import tn.oliveconnect.common.dto.PageResponse;
import tn.oliveconnect.common.dto.Paging;
import tn.oliveconnect.common.exception.NotFoundException;
import tn.oliveconnect.m3_ressources.dto.BenneRequest;
import tn.oliveconnect.m3_ressources.dto.BenneResponse;
import tn.oliveconnect.m3_ressources.entity.Benne;
import tn.oliveconnect.m3_ressources.entity.TypeRessource;
import tn.oliveconnect.m3_ressources.repository.BenneRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BenneService {

    private final BenneRepository repository;
    private final RechercheDisponibiliteService rechercheDisponibiliteService;

    public BenneResponse creer(BenneRequest request) {
        Benne benne = new Benne();
        appliquer(benne, request);
        return BenneResponse.from(repository.save(benne));
    }

    public boolean existeImmatriculation(String immatriculation) {
        return repository.existsByImmatriculationEquivalent(
                ImmatriculationNormalizer.regexEquivalent(immatriculation));
    }

    public PageResponse<BenneResponse> lister(int page, int size) {
        Pageable pageable = Paging.of(page, size, Sort.by(Sort.Direction.ASC, "nom"));
        Page<BenneResponse> result = repository.findAll(pageable).map(BenneResponse::from);
        return PageResponse.from(result);
    }

    /** La disponibilité porte sur l'état enregistré; les affectations futures ne sont pas encore vérifiées. */
    public PageResponse<BenneResponse> disponibles(LocalDateTime debut, LocalDateTime fin, int page, int size) {
        PeriodeDisponibilite.valider(debut, fin);
        Pageable pageable = Paging.of(page, size, Sort.by(Sort.Direction.ASC, "nom"));
        List<Benne> candidats = repository.findByDisponibiliteTrue(pageable.getSort());
        return rechercheDisponibiliteService.filtrer(candidats, TypeRessource.BENNE, debut, fin,
                pageable, BenneResponse::from);
    }

    public BenneResponse obtenir(String id) {
        return BenneResponse.from(trouver(id));
    }

    public BenneResponse modifier(String id, BenneRequest request) {
        Benne benne = trouver(id);
        appliquer(benne, request);
        return BenneResponse.from(repository.save(benne));
    }

    public void supprimer(String id) {
        trouver(id);
        repository.deleteById(id);
    }

    private Benne trouver(String id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Benne introuvable : " + id));
    }

    private void appliquer(Benne benne, BenneRequest request) {
        benne.setNom(request.nom().trim());
        benne.setDisponibilite(request.disponibilite());
        benne.setCapacite(request.capacite());
        benne.setImmatriculation(request.immatriculation().trim());
    }
}
