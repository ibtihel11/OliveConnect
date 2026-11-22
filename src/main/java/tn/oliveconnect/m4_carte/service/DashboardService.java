package tn.oliveconnect.m4_carte.service;

import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.stereotype.Service;
import tn.oliveconnect.m4_carte.dto.DelaiCollecteResponse;
import tn.oliveconnect.m4_carte.dto.DelaiParTypeResponse;
import tn.oliveconnect.m4_carte.dto.DelaiParVergerResponse;
import tn.oliveconnect.m4_carte.dto.RemplissageBenneResponse;
import tn.oliveconnect.m4_carte.dto.RemplissageBennesResponse;
import tn.oliveconnect.m4_carte.dto.RendementVergerResponse;
import tn.oliveconnect.m4_carte.repository.DashboardRepository;

import java.util.List;

/** Tableau de bord : indicateurs calculés par agrégation, arrondis à 2 décimales. */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final DashboardRepository dashboardRepository;

    public List<RendementVergerResponse> rendement() {
        return dashboardRepository.rendementParVerger().stream()
                .map(d -> new RendementVergerResponse(
                        Docs.str(d, "vergerId"),
                        Docs.str(d, "nomVerger"),
                        Docs.num(d, "superficie"),
                        Docs.round2(Docs.num(d, "quantiteRecoltee")),
                        Docs.round2(Docs.num(d, "poidsRecu")),
                        Docs.round2(Docs.num(d, "rendementKgParHa")),
                        Docs.round2(Docs.num(d, "tauxReception")),
                        Docs.lng(d, "nombreTournees")))
                .toList();
    }

    public DelaiCollecteResponse delais() {
        Document facet = dashboardRepository.delaisCollecte();

        List<Document> global = Docs.documents(facet, "global");
        long n = 0;
        Double moyenne = null;
        Double min = null;
        Double max = null;
        if (!global.isEmpty()) {
            Document g = global.get(0);
            n = Docs.lng(g, "n");
            moyenne = Docs.round2(Docs.num(g, "moyenne"));
            min = Docs.round2(Docs.num(g, "min"));
            max = Docs.round2(Docs.num(g, "max"));
        }

        List<DelaiParTypeResponse> parType = Docs.documents(facet, "parType").stream()
                .map(d -> new DelaiParTypeResponse(
                        Boolean.TRUE.equals(d.get("_id")),
                        Docs.lng(d, "n"),
                        Docs.round2(Docs.num(d, "moyenne"))))
                .toList();

        List<DelaiParVergerResponse> parVerger = Docs.documents(facet, "parVerger").stream()
                .map(d -> new DelaiParVergerResponse(
                        Docs.str(d, "vergerId"),
                        Docs.str(d, "nomVerger"),
                        Docs.lng(d, "n"),
                        Docs.round2(Docs.num(d, "moyenne"))))
                .toList();

        return new DelaiCollecteResponse(n, moyenne, min, max, parType, parVerger);
    }

    public RemplissageBennesResponse remplissageBennes() {
        List<Document> docs = dashboardRepository.remplissageParBenne();

        List<RemplissageBenneResponse> parBenne = docs.stream()
                .map(d -> new RemplissageBenneResponse(
                        Docs.str(d, "benneId"),
                        Docs.str(d, "immatriculation"),
                        Docs.num(d, "capaciteKg"),
                        Docs.lng(d, "nombreReceptions"),
                        Docs.round2(Docs.num(d, "poidsMoyenKg")),
                        Docs.round2(Docs.num(d, "tauxRemplissageMoyen")),
                        Docs.round2(Docs.num(d, "tauxRemplissageMax"))))
                .toList();

        // Moyenne globale pondérée par le nombre de réceptions de chaque benne
        long total = 0;
        double somme = 0;
        for (Document d : docs) {
            long nb = Docs.lng(d, "nombreReceptions");
            Double taux = Docs.num(d, "tauxRemplissageMoyen");
            if (taux != null) {
                total += nb;
                somme += taux * nb;
            }
        }
        Double tauxGlobal = total == 0 ? null : Docs.round2(somme / total);
        return new RemplissageBennesResponse(total, tauxGlobal, parBenne);
    }
}
