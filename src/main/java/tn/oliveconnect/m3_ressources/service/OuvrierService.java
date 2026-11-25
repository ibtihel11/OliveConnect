package tn.oliveconnect.m3_ressources.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import tn.oliveconnect.common.dto.PageResponse;
import tn.oliveconnect.common.dto.Paging;
import tn.oliveconnect.common.exception.NotFoundException;
import tn.oliveconnect.m3_ressources.dto.OuvrierRequest;
import tn.oliveconnect.m3_ressources.dto.OuvrierResponse;
import tn.oliveconnect.m3_ressources.entity.Ouvrier;
import tn.oliveconnect.m3_ressources.entity.TypeRessource;
import tn.oliveconnect.m3_ressources.repository.OuvrierRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OuvrierService {

    private final OuvrierRepository repository;
    private final RechercheDisponibiliteService rechercheDisponibiliteService;

    public OuvrierResponse creer(OuvrierRequest request) {
        Ouvrier ouvrier = new Ouvrier();
        appliquer(ouvrier, request);
        return OuvrierResponse.from(repository.save(ouvrier));
    }

    public PageResponse<OuvrierResponse> lister(int page, int size) {
        Pageable pageable = Paging.of(page, size, Sort.by(Sort.Direction.ASC, "nom"));
        Page<OuvrierResponse> result = repository.findAll(pageable).map(OuvrierResponse::from);
        return PageResponse.from(result);
    }

    /** La disponibilité porte sur l'état enregistré; les affectations futures ne sont pas encore vérifiées. */
    public PageResponse<OuvrierResponse> disponibles(LocalDateTime debut, LocalDateTime fin, int page, int size) {
        PeriodeDisponibilite.valider(debut, fin);
        Pageable pageable = Paging.of(page, size, Sort.by(Sort.Direction.ASC, "nom"));
        List<Ouvrier> candidats = repository.findByDisponibiliteTrue(pageable.getSort());
        return rechercheDisponibiliteService.filtrer(candidats, TypeRessource.OUVRIER, debut, fin,
                pageable, OuvrierResponse::from);
    }

    public OuvrierResponse obtenir(String id) {
        return OuvrierResponse.from(trouver(id));
    }

    public OuvrierResponse modifier(String id, OuvrierRequest request) {
        Ouvrier ouvrier = trouver(id);
        appliquer(ouvrier, request);
        return OuvrierResponse.from(repository.save(ouvrier));
    }

    public void supprimer(String id) {
        trouver(id);
        repository.deleteById(id);
    }

    private Ouvrier trouver(String id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Ouvrier introuvable : " + id));
    }

    private void appliquer(Ouvrier ouvrier, OuvrierRequest request) {
        ouvrier.setNom(request.nom().trim());
        ouvrier.setDisponibilite(request.disponibilite());
        ouvrier.setSpecialite(request.specialite().trim());
        ouvrier.setSaisonnier(request.saisonnier());
    }
}
