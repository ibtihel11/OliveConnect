package tn.oliveconnect.m3_ressources.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import tn.oliveconnect.common.dto.PageResponse;
import tn.oliveconnect.common.dto.Paging;
import tn.oliveconnect.common.exception.NotFoundException;
import tn.oliveconnect.m3_ressources.dto.TracteurRequest;
import tn.oliveconnect.m3_ressources.dto.TracteurResponse;
import tn.oliveconnect.m3_ressources.entity.Tracteur;
import tn.oliveconnect.m3_ressources.entity.TypeRessource;
import tn.oliveconnect.m3_ressources.repository.TracteurRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TracteurService {

    private final TracteurRepository repository;
    private final RechercheDisponibiliteService rechercheDisponibiliteService;

    public TracteurResponse creer(TracteurRequest request) {
        Tracteur tracteur = new Tracteur();
        appliquer(tracteur, request);
        return TracteurResponse.from(repository.save(tracteur));
    }

    public boolean existeImmatriculation(String immatriculation) {
        return repository.existsByImmatriculationEquivalent(
                ImmatriculationNormalizer.regexEquivalent(immatriculation));
    }

    public PageResponse<TracteurResponse> lister(int page, int size) {
        Pageable pageable = Paging.of(page, size, Sort.by(Sort.Direction.ASC, "nom"));
        Page<TracteurResponse> result = repository.findAll(pageable).map(TracteurResponse::from);
        return PageResponse.from(result);
    }

    /** La disponibilité porte sur l'état enregistré; les affectations futures ne sont pas encore vérifiées. */
    public PageResponse<TracteurResponse> disponibles(LocalDateTime debut, LocalDateTime fin, int page, int size) {
        PeriodeDisponibilite.valider(debut, fin);
        Pageable pageable = Paging.of(page, size, Sort.by(Sort.Direction.ASC, "nom"));
        List<Tracteur> candidats = repository.findByDisponibiliteTrue(pageable.getSort());
        return rechercheDisponibiliteService.filtrer(candidats, TypeRessource.TRACTEUR, debut, fin,
                pageable, TracteurResponse::from);
    }

    public TracteurResponse obtenir(String id) {
        return TracteurResponse.from(trouver(id));
    }

    public TracteurResponse modifier(String id, TracteurRequest request) {
        Tracteur tracteur = trouver(id);
        appliquer(tracteur, request);
        return TracteurResponse.from(repository.save(tracteur));
    }

    public void supprimer(String id) {
        trouver(id);
        repository.deleteById(id);
    }

    private Tracteur trouver(String id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Tracteur introuvable : " + id));
    }

    private void appliquer(Tracteur tracteur, TracteurRequest request) {
        tracteur.setNom(request.nom().trim());
        tracteur.setDisponibilite(request.disponibilite());
        tracteur.setImmatriculation(request.immatriculation().trim());
        tracteur.setPuissance(request.puissance());
    }
}
