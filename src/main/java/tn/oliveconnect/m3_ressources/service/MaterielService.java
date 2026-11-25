package tn.oliveconnect.m3_ressources.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import tn.oliveconnect.common.dto.PageResponse;
import tn.oliveconnect.common.dto.Paging;
import tn.oliveconnect.common.exception.NotFoundException;
import tn.oliveconnect.m3_ressources.dto.MaterielRequest;
import tn.oliveconnect.m3_ressources.dto.MaterielResponse;
import tn.oliveconnect.m3_ressources.entity.Materiel;
import tn.oliveconnect.m3_ressources.entity.TypeRessource;
import tn.oliveconnect.m3_ressources.repository.MaterielRepository;

import java.util.List;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MaterielService {

    private final MaterielRepository repository;
    private final RechercheDisponibiliteService rechercheDisponibiliteService;

    public MaterielResponse creer(MaterielRequest request) {
        Materiel materiel = new Materiel();
        appliquer(materiel, request);
        return MaterielResponse.from(repository.save(materiel));
    }

    public PageResponse<MaterielResponse> lister(int page, int size) {
        Pageable pageable = Paging.of(page, size, Sort.by(Sort.Direction.ASC, "nom"));
        Page<MaterielResponse> result = repository.findAll(pageable).map(MaterielResponse::from);
        return PageResponse.from(result);
    }

    /** La disponibilité porte sur l'état enregistré; les affectations futures ne sont pas encore vérifiées. */
    public PageResponse<MaterielResponse> disponibles(LocalDateTime debut, LocalDateTime fin, int page, int size) {
        PeriodeDisponibilite.valider(debut, fin);
        Pageable pageable = Paging.of(page, size, Sort.by(Sort.Direction.ASC, "nom"));
        List<Materiel> candidats = repository.findByDisponibiliteTrue(pageable.getSort());
        return rechercheDisponibiliteService.filtrer(candidats, TypeRessource.MATERIEL, debut, fin,
                pageable, MaterielResponse::from);
    }

    /** Retourne les matériels au seuil ou sous le seuil défini, tous types confondus. */
    public List<MaterielResponse> listerStocksBas() {
        return repository.findAll(Sort.by(Sort.Direction.ASC, "nom")).stream()
                .filter(this::estEnStockBas)
                .map(MaterielResponse::from)
                .toList();
    }

    public MaterielResponse obtenir(String id) {
        return MaterielResponse.from(trouver(id));
    }

    public MaterielResponse modifier(String id, MaterielRequest request) {
        Materiel materiel = trouver(id);
        appliquer(materiel, request);
        return MaterielResponse.from(repository.save(materiel));
    }

    public void supprimer(String id) {
        trouver(id);
        repository.deleteById(id);
    }

    private Materiel trouver(String id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Matériel introuvable : " + id));
    }

    private void appliquer(Materiel materiel, MaterielRequest request) {
        materiel.setNom(request.nom().trim());
        materiel.setDisponibilite(request.disponibilite());
        materiel.setType(request.type().trim());
        materiel.setQuantiteStock(request.quantiteStock());
        materiel.setSeuilMinimum(request.seuilMinimum());
    }

    private boolean estEnStockBas(Materiel materiel) {
        return materiel.getQuantiteStock() != null
                && materiel.getSeuilMinimum() != null
                && materiel.getQuantiteStock() <= materiel.getSeuilMinimum();
    }
}
