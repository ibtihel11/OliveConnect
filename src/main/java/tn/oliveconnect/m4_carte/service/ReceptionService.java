package tn.oliveconnect.m4_carte.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import tn.oliveconnect.common.dto.PageResponse;
import tn.oliveconnect.common.dto.Paging;
import tn.oliveconnect.common.exception.BusinessConflictException;
import tn.oliveconnect.common.exception.ForbiddenException;
import tn.oliveconnect.common.exception.NotFoundException;
import tn.oliveconnect.m4_carte.dto.ReceptionRequest;
import tn.oliveconnect.m4_carte.dto.ReceptionResponse;
import tn.oliveconnect.m4_carte.entity.Pressoir;
import tn.oliveconnect.m4_carte.entity.Reception;
import tn.oliveconnect.m4_carte.integration.BenneReader;
import tn.oliveconnect.m4_carte.integration.TourneeReader;
import tn.oliveconnect.m4_carte.integration.TourneeRef;
import tn.oliveconnect.m4_carte.repository.PressoirRepository;
import tn.oliveconnect.m4_carte.repository.ReceptionRepository;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Set;

/** Réception au pressoir (F5) : règles métier de M4. */
@Service
@RequiredArgsConstructor
public class ReceptionService {

    /** Une réception n'a de sens que pour une tournée démarrée. */
    static final Set<String> STATUTS_TOURNEE_ACCEPTES = Set.of("EN_COURS", "TERMINEE");

    private final ReceptionRepository receptionRepository;
    private final PressoirRepository pressoirRepository;
    private final TourneeReader tourneeReader;
    private final BenneReader benneReader;
    private final Clock clock;

    /**
     * Enregistre la réception d'une benne.
     * 404 : pressoir, benne ou tournée inexistants. 403 : pressoir géré par un autre responsable.
     * 409 : tournée non démarrée (PLANIFIEE) ou annulée. 400 (poids négatif) : validation du DTO en amont.
     */
    public ReceptionResponse enregistrer(ReceptionRequest request, String responsableId) {
        Pressoir pressoir = pressoirRepository.findById(request.pressoirId())
                .orElseThrow(() -> new NotFoundException("Pressoir introuvable : " + request.pressoirId()));
        if (!responsableId.equals(pressoir.getResponsableId())) {
            throw new ForbiddenException("Ce pressoir n'est pas géré par l'utilisateur connecté");
        }
        benneReader.findById(request.benneId())
                .orElseThrow(() -> new NotFoundException("Benne introuvable : " + request.benneId()));
        TourneeRef tournee = tourneeReader.findById(request.tourneeId())
                .orElseThrow(() -> new NotFoundException("Tournée introuvable : " + request.tourneeId()));
        if (tournee.statut() == null || !STATUTS_TOURNEE_ACCEPTES.contains(tournee.statut())) {
            throw new BusinessConflictException(
                    "Réception impossible : la tournée " + tournee.id() + " n'est pas démarrée (statut " + tournee.statut() + ")");
        }

        Reception reception = Reception.builder()
                .tourneeId(request.tourneeId())
                .benneId(request.benneId())
                .pressoirId(request.pressoirId())
                .dateReception(LocalDateTime.now(clock))
                .poids(request.poids())
                .qualite(request.qualite())
                .build();
        return ReceptionResponse.from(receptionRepository.save(reception));
    }

    /** Liste paginée, plus récentes d'abord, filtrable par tournée. */
    public PageResponse<ReceptionResponse> lister(String tourneeId, int page, int size) {
        Pageable pageable = Paging.of(page, size, Sort.by(Sort.Direction.DESC, "dateReception"));
        Page<Reception> result = (tourneeId == null || tourneeId.isBlank())
                ? receptionRepository.findAll(pageable)
                : receptionRepository.findByTourneeId(tourneeId, pageable);
        return PageResponse.from(result.map(ReceptionResponse::from));
    }
}
