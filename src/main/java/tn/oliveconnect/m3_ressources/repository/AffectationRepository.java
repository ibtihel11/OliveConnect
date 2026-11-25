package tn.oliveconnect.m3_ressources.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import tn.oliveconnect.m3_ressources.entity.Affectation;
import tn.oliveconnect.m3_ressources.entity.TypeRessource;

import java.time.LocalDateTime;
import java.util.List;

public interface AffectationRepository extends MongoRepository<Affectation, String> {

    boolean existsByRessourceIdAndRessourceTypeAndStatutNotInAndDateDebutLessThanAndDateFinGreaterThan(
            String ressourceId, TypeRessource ressourceType, List<String> statutsInactifs,
            LocalDateTime dateFin, LocalDateTime dateDebut);

    List<Affectation> findByRessourceTypeAndStatutNotInAndDateDebutLessThanAndDateFinGreaterThan(
            TypeRessource ressourceType, List<String> statutsInactifs,
            LocalDateTime dateFin, LocalDateTime dateDebut);

    Page<Affectation> findByRessourceIdAndRessourceType(
            String ressourceId, TypeRessource ressourceType, Pageable pageable);

    List<Affectation> findByRessourceIdAndRessourceTypeOrderByDateDebutAsc(
            String ressourceId, TypeRessource ressourceType);
}
