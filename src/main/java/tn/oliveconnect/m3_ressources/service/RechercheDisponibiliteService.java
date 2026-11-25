package tn.oliveconnect.m3_ressources.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import tn.oliveconnect.common.dto.PageResponse;
import tn.oliveconnect.m3_ressources.entity.Ressource;
import tn.oliveconnect.m3_ressources.entity.TypeRessource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;

/** Filtre les ressources selon leur état enregistré et les affectations M3 qui chevauchent la période. */
@Service
@RequiredArgsConstructor
public class RechercheDisponibiliteService {

    private final AffectationService affectationService;

    public <T extends Ressource, D> PageResponse<D> filtrer(List<T> ressourcesDisponibles,
                                                             TypeRessource type,
                                                             LocalDateTime debut,
                                                             LocalDateTime fin,
                                                             Pageable pageable,
                                                             Function<T, D> mapper) {
        var occupees = affectationService.ressourcesEnConflit(type, debut, fin);
        List<D> resultats = ressourcesDisponibles.stream()
                .filter(ressource -> Boolean.TRUE.equals(ressource.getDisponibilite()))
                .filter(ressource -> !occupees.contains(ressource.getId()))
                .map(mapper)
                .toList();

        int debutPage = (int) Math.min((long) pageable.getPageNumber() * pageable.getPageSize(), resultats.size());
        int finPage = Math.min(debutPage + pageable.getPageSize(), resultats.size());
        return PageResponse.from(new PageImpl<>(resultats.subList(debutPage, finPage), pageable, resultats.size()));
    }
}
