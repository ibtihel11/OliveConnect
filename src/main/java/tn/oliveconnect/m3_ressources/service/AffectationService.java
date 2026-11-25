package tn.oliveconnect.m3_ressources.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import tn.oliveconnect.common.dto.PageResponse;
import tn.oliveconnect.common.dto.Paging;
import tn.oliveconnect.common.exception.BusinessConflictException;
import tn.oliveconnect.common.exception.NotFoundException;
import tn.oliveconnect.m3_ressources.dto.AffectationRequest;
import tn.oliveconnect.m3_ressources.dto.AffectationResponse;
import tn.oliveconnect.m3_ressources.entity.Affectation;
import tn.oliveconnect.m3_ressources.entity.Ressource;
import tn.oliveconnect.m3_ressources.entity.TypeRessource;
import tn.oliveconnect.m3_ressources.repository.AffectationRepository;
import tn.oliveconnect.m3_ressources.repository.BenneRepository;
import tn.oliveconnect.m3_ressources.repository.MaterielRepository;
import tn.oliveconnect.m3_ressources.repository.OuvrierRepository;
import tn.oliveconnect.m3_ressources.repository.TracteurRepository;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AffectationService {

    private static final List<String> STATUTS_INACTIFS = List.of("ANNULEE", "TERMINEE");

    private final AffectationRepository affectationRepository;
    private final OuvrierRepository ouvrierRepository;
    private final TracteurRepository tracteurRepository;
    private final BenneRepository benneRepository;
    private final MaterielRepository materielRepository;
    private final VerrouAffectationRessource verrouAffectationRessource;

    public AffectationResponse creer(AffectationRequest request) {
        PeriodeDisponibilite.valider(request.dateDebut(), request.dateFin());
        Ressource ressource = trouverRessource(request.ressourceType(), request.ressourceId());
        if (!Boolean.TRUE.equals(ressource.getDisponibilite())) {
            throw new BusinessConflictException("La ressource est actuellement indisponible");
        }

        String statut = request.statut().trim().toUpperCase(Locale.ROOT);
        String ressourceId = request.ressourceId().trim();
        return verrouAffectationRessource.executer(request.ressourceType(), ressourceId,
                () -> creerSousVerrou(request, statut, ressourceId));
    }

    /** La vérification et la sauvegarde sont sérialisées pour le couple (type, ID) dans cette instance. */
    private AffectationResponse creerSousVerrou(AffectationRequest request, String statut, String ressourceId) {
        if (!STATUTS_INACTIFS.contains(statut)) {
            boolean chevauchement = affectationRepository
                    .existsByRessourceIdAndRessourceTypeAndStatutNotInAndDateDebutLessThanAndDateFinGreaterThan(
                            request.ressourceId(), request.ressourceType(), STATUTS_INACTIFS,
                            request.dateFin(), request.dateDebut());
            if (chevauchement) {
                throw new BusinessConflictException("La ressource possède déjà une affectation active sur cette période");
            }
        }

        Affectation affectation = new Affectation();
        affectation.setRessourceId(ressourceId);
        affectation.setRessourceType(request.ressourceType());
        // tourneeId reste une référence externe; M1 n'est pas interrogé.
        affectation.setTourneeId(request.tourneeId().trim());
        affectation.setDateDebut(request.dateDebut());
        affectation.setDateFin(request.dateFin());
        affectation.setStatut(statut);
        return AffectationResponse.from(affectationRepository.save(affectation));
    }

    public void supprimer(String id) {
        trouverAffectation(id);
        affectationRepository.deleteById(id);
    }

    public PageResponse<AffectationResponse> listerParRessource(
            String ressourceId, TypeRessource ressourceType, int page, int size) {
        Pageable pageable = Paging.of(page, size, Sort.by(Sort.Direction.ASC, "dateDebut"));
        Page<AffectationResponse> result = affectationRepository
                .findByRessourceIdAndRessourceType(ressourceId, ressourceType, pageable)
                .map(AffectationResponse::from);
        return PageResponse.from(result);
    }

    public List<AffectationResponse> listerPourCalendrier(String ressourceId, TypeRessource ressourceType) {
        return affectationRepository.findByRessourceIdAndRessourceTypeOrderByDateDebutAsc(ressourceId, ressourceType)
                .stream().map(AffectationResponse::from).toList();
    }

    /** Retourne les identifiants occupés sur la période, en appliquant les mêmes statuts et bornes que la création. */
    public Set<String> ressourcesEnConflit(TypeRessource ressourceType,
                                           java.time.LocalDateTime debut,
                                           java.time.LocalDateTime fin) {
        PeriodeDisponibilite.valider(debut, fin);
        return affectationRepository
                .findByRessourceTypeAndStatutNotInAndDateDebutLessThanAndDateFinGreaterThan(
                        ressourceType, STATUTS_INACTIFS, fin, debut)
                .stream()
                .map(Affectation::getRessourceId)
                .collect(Collectors.toSet());
    }

    private Ressource trouverRessource(TypeRessource type, String id) {
        return switch (type) {
            case OUVRIER -> ouvrierRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("Ouvrier introuvable : " + id));
            case TRACTEUR -> tracteurRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("Tracteur introuvable : " + id));
            case BENNE -> benneRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("Benne introuvable : " + id));
            case MATERIEL -> materielRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("Matériel introuvable : " + id));
        };
    }

    private Affectation trouverAffectation(String id) {
        return affectationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Affectation introuvable : " + id));
    }
}
