package tn.oliveconnect.m3_ressources.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.oliveconnect.common.exception.BadRequestException;
import tn.oliveconnect.common.exception.NotFoundException;
import tn.oliveconnect.m3_ressources.dto.CalendrierRessourceResponse;
import tn.oliveconnect.m3_ressources.dto.CalendrierAffectationResponse;
import tn.oliveconnect.m3_ressources.dto.PeriodeCalendrierResponse;
import tn.oliveconnect.m3_ressources.entity.Benne;
import tn.oliveconnect.m3_ressources.entity.Materiel;
import tn.oliveconnect.m3_ressources.entity.Ouvrier;
import tn.oliveconnect.m3_ressources.entity.Ressource;
import tn.oliveconnect.m3_ressources.entity.Tracteur;
import tn.oliveconnect.m3_ressources.entity.TypeRessource;
import tn.oliveconnect.m3_ressources.repository.BenneRepository;
import tn.oliveconnect.m3_ressources.repository.MaterielRepository;
import tn.oliveconnect.m3_ressources.repository.OuvrierRepository;
import tn.oliveconnect.m3_ressources.repository.TracteurRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class CalendrierService {

    private final AffectationService affectationService;

    private final OuvrierRepository ouvrierRepository;
    private final TracteurRepository tracteurRepository;
    private final BenneRepository benneRepository;
    private final MaterielRepository materielRepository;

    /**
     * Consulte les données M3 enregistrées : la disponibilité reflète le booléen de la ressource et les
     * affectations retournées proviennent de la collection M3 {@code affectations}. Elles sont toutes incluses
     * sans période, ou filtrées par chevauchement semi-ouvert avec la période demandée. Les détails de tournée
     * M1 ne sont pas consultés; {@code tourneeId} reste une référence opaque et n'est pas exposé au calendrier.
     */
    public CalendrierRessourceResponse consulter(String ressourceId, LocalDateTime debut, LocalDateTime fin) {
        if (debut == null ^ fin == null) {
            throw new BadRequestException("Les paramètres debut et fin doivent être fournis ensemble");
        }
        PeriodeCalendrierResponse periode = null;
        if (debut != null) {
            PeriodeDisponibilite.valider(debut, fin);
            periode = new PeriodeCalendrierResponse(debut, fin);
        }

        List<Ressource> trouvees = new ArrayList<>(4);
        ouvrierRepository.findById(ressourceId).ifPresent(trouvees::add);
        tracteurRepository.findById(ressourceId).ifPresent(trouvees::add);
        benneRepository.findById(ressourceId).ifPresent(trouvees::add);
        materielRepository.findById(ressourceId).ifPresent(trouvees::add);

        if (trouvees.isEmpty()) {
            throw new NotFoundException("Ressource introuvable : " + ressourceId);
        }
        if (trouvees.size() > 1) {
            throw new BadRequestException("Identifiant de ressource ambigu : " + ressourceId);
        }
        return convertir(trouvees.get(0), periode, debut, fin);
    }

    private CalendrierRessourceResponse convertir(Ressource ressource, PeriodeCalendrierResponse periode,
                                                  LocalDateTime debut, LocalDateTime fin) {
        String type = switch (ressource) {
            case Ouvrier ignored -> "ouvrier";
            case Tracteur ignored -> "tracteur";
            case Benne ignored -> "benne";
            case Materiel ignored -> "materiel";
            default -> throw new IllegalStateException("Type de ressource non pris en charge");
        };
        TypeRessource typeEnum = TypeRessource.valueOf(type.toUpperCase(Locale.ROOT));
        List<CalendrierAffectationResponse> affectations = affectationService
                .listerPourCalendrier(ressource.getId(), typeEnum).stream()
                .filter(affectation -> debut == null ||
                        (affectation.dateDebut().isBefore(fin) && affectation.dateFin().isAfter(debut)))
                .map(affectation -> new CalendrierAffectationResponse(affectation.id(), affectation.dateDebut(),
                        affectation.dateFin(), affectation.statut()))
                .toList();
        return new CalendrierRessourceResponse(
                ressource.getId(), ressource.getNom(), type, ressource.getDisponibilite(), periode, affectations);
    }
}
