package tn.oliveconnect.m4_carte.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;
import tn.oliveconnect.m4_carte.dto.MesureCapteurRequest;
import tn.oliveconnect.m4_carte.dto.ReleveRecentResponse;
import tn.oliveconnect.m4_carte.entity.Pressoir;
import tn.oliveconnect.m4_carte.repository.CarteRepository;
import tn.oliveconnect.m4_carte.repository.PressoirRepository;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Systèmes externes simulés (cadrage, acteurs) :
 * - service météo / capteurs : émet un JSON de mesures, passé au service d'ingestion ;
 * - système du pressoir : fait varier la capacité de traitement en "temps réel".
 * Actif seulement si oliveconnect.simulation.enabled=true (profil dev).
 */
@Service
@ConditionalOnProperty(name = "oliveconnect.simulation.enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class SimulationService {

    private final CarteRepository carteRepository;
    private final PressoirRepository pressoirRepository;
    private final MesureIngestionService ingestionService;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    private final Random random = new Random();
    private final Map<String, Double> capaciteDeBase = new ConcurrentHashMap<>();

    @Scheduled(fixedDelayString = "${oliveconnect.simulation.interval-ms:60000}",
            initialDelayString = "${oliveconnect.simulation.initial-delay-ms:10000}")
    public void emettreMesuresCapteurs() {
        try {
            List<MesureCapteurRequest> mesures = new ArrayList<>();
            LocalDateTime maintenant = LocalDateTime.now(clock);
            for (Document verger : carteRepository.findVergers()) {
                ReleveRecentResponse dernier = Docs.dernierReleve(verger);
                double base = dernier != null && dernier.niveauMaturite() != null
                        ? dernier.niveauMaturite() : 20 + random.nextDouble() * 20;
                double maturite = Math.min(100, base + random.nextDouble() * 2);
                double pluie = random.nextDouble() < 0.25 ? random.nextDouble() * 8 : 0;
                mesures.add(new MesureCapteurRequest(
                        Docs.id(verger), maintenant,
                        arrondi(12 + random.nextDouble() * 20),
                        arrondi(35 + random.nextDouble() * 50),
                        arrondi(pluie),
                        arrondi(maturite)));
            }
            if (!mesures.isEmpty()) {
                // Le système externe parle JSON : on sérialise puis on ingère comme un vrai flux.
                ingestionService.ingererJson(objectMapper.writeValueAsString(mesures));
            }
        } catch (RuntimeException e) {
            log.error("Simulation capteurs en échec", e);
        }
    }

    @Scheduled(fixedDelayString = "${oliveconnect.simulation.interval-ms:60000}",
            initialDelayString = "${oliveconnect.simulation.initial-delay-ms:10000}")
    public void emettreCapacitesPressoirs() {
        try {
            for (Pressoir p : pressoirRepository.findAll()) {
                if (p.getCapaciteTraitement() == null) {
                    continue;
                }
                double base = capaciteDeBase.computeIfAbsent(p.getId(), id -> p.getCapaciteTraitement());
                p.setCapaciteTraitement(arrondi(base * (0.9 + random.nextDouble() * 0.2)));
                pressoirRepository.save(p);
            }
        } catch (RuntimeException e) {
            log.error("Simulation capacité des pressoirs en échec", e);
        }
    }

    private static double arrondi(double v) {
        return Math.round(v * 10.0) / 10.0;
    }
}
