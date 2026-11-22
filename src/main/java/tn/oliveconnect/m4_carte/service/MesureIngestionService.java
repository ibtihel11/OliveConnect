package tn.oliveconnect.m4_carte.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tn.oliveconnect.common.exception.BadRequestException;
import tn.oliveconnect.m4_carte.dto.IngestionBilan;
import tn.oliveconnect.m4_carte.dto.MesureCapteurRequest;
import tn.oliveconnect.m4_carte.entity.DonneeMeteo;
import tn.oliveconnect.m4_carte.entity.ReleveMaturite;
import tn.oliveconnect.m4_carte.repository.DonneeMeteoRepository;
import tn.oliveconnect.m4_carte.repository.ReleveMaturiteRepository;
import tn.oliveconnect.m4_carte.repository.VergerReleveRepository;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Intégration des données dynamiques (service météo / capteurs, JSON) : contrôle de conformité,
 * journalisation des rejets, enregistrement dans meteo et releves_maturite, et mise à jour des
 * 5 derniers relevés imbriqués dans le verger (pour la carte, sans jointure).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MesureIngestionService {

    private final DonneeMeteoRepository meteoRepository;
    private final ReleveMaturiteRepository releveRepository;
    private final VergerReleveRepository vergerReleveRepository;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public IngestionBilan ingererJson(String json) {
        List<MesureCapteurRequest> mesures;
        try {
            mesures = objectMapper.readValue(json, new TypeReference<List<MesureCapteurRequest>>() {
            });
        } catch (JacksonException e) {
            throw new BadRequestException("JSON capteurs invalide : " + e.getOriginalMessage());
        }
        return ingerer(mesures);
    }

    public IngestionBilan ingerer(List<MesureCapteurRequest> mesures) {
        int acceptees = 0;
        int rejetees = 0;
        for (MesureCapteurRequest m : mesures) {
            Optional<String> erreur = valider(m);
            if (erreur.isEmpty() && !vergerReleveRepository.existe(m.vergerId())) {
                erreur = Optional.of("verger inexistant");
            }
            if (erreur.isPresent()) {
                log.warn("Mesure rejetée (verger={}) : {}", m == null ? null : m.vergerId(), erreur.get());
                rejetees++;
                continue;
            }

            LocalDateTime date = m.dateReleve() != null ? m.dateReleve() : LocalDateTime.now(clock);
            if (m.temperature() != null || m.humidite() != null || m.precipitation() != null) {
                meteoRepository.save(DonneeMeteo.builder()
                        .vergerId(m.vergerId()).dateReleve(date)
                        .temperature(m.temperature()).humidite(m.humidite()).precipitation(m.precipitation())
                        .build());
            }
            if (m.niveauMaturite() != null) {
                releveRepository.save(ReleveMaturite.builder()
                        .vergerId(m.vergerId()).dateReleve(date).niveauMaturite(m.niveauMaturite())
                        .build());
                vergerReleveRepository.pousserDernierReleve(m.vergerId(), date, m.niveauMaturite());
            }
            acceptees++;
        }
        log.info("Ingestion capteurs : {} acceptée(s), {} rejetée(s)", acceptees, rejetees);
        return new IngestionBilan(acceptees, rejetees);
    }

    static Optional<String> valider(MesureCapteurRequest m) {
        if (m == null) {
            return Optional.of("mesure vide");
        }
        if (m.vergerId() == null || m.vergerId().isBlank()) {
            return Optional.of("vergerId manquant");
        }
        if (m.temperature() == null && m.humidite() == null && m.precipitation() == null && m.niveauMaturite() == null) {
            return Optional.of("aucune valeur mesurée");
        }
        if (m.temperature() != null && (m.temperature() < -50 || m.temperature() > 60)) {
            return Optional.of("température hors de [-50, 60] °C");
        }
        if (m.humidite() != null && (m.humidite() < 0 || m.humidite() > 100)) {
            return Optional.of("humidité hors de [0, 100] %");
        }
        if (m.precipitation() != null && m.precipitation() < 0) {
            return Optional.of("précipitation négative");
        }
        if (m.niveauMaturite() != null && (m.niveauMaturite() < 0 || m.niveauMaturite() > 100)) {
            return Optional.of("niveau de maturité hors de [0, 100] %");
        }
        return Optional.empty();
    }
}
