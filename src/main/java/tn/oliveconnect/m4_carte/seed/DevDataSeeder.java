package tn.oliveconnect.m4_carte.seed;

import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Données de démonstration (profil dev). Chaque collection n'est remplie que si elle est vide :
 * - M4 : pressoirs, receptions, releves_maturite, meteo (données réelles de M4) ;
 * - MOCKS des dépendances non encore livrées (vergers, varietes, recoltes, tournees, bennes) :
 *   à l'arrivée des vraies données de M1/M3 ces collections ne sont plus touchées.
 * Les ids reprennent l'exemple du cadrage (t7, b3, p1).
 */
@Component
@Profile("dev")
@ConditionalOnProperty(name = "oliveconnect.seed.enabled", havingValue = "true", matchIfMissing = true)
@Slf4j
public class DevDataSeeder implements CommandLineRunner {

    private final MongoTemplate mongo;
    private final LocalDateTime now = LocalDateTime.now();

    public DevDataSeeder(MongoTemplate mongo) {
        this.mongo = mongo;
    }

    @Override
    public void run(String... args) {
        seedMocksDeM1M3();
        seedDonneesM4();
    }

    // ------------------------------------------------------------------ mocks M1 / M3

    private void seedMocksDeM1M3() {
        seed("varietes", List.of(
                new Document("_id", "var1").append("nom", "Chemlali").append("description", "Variété dominante du Sahel et de Sfax"),
                new Document("_id", "var2").append("nom", "Chétoui").append("description", "Variété du nord, huile fruitée"),
                new Document("_id", "var3").append("nom", "Arbequina").append("description", "Variété intensive")));

        seed("vergers", List.of(
                verger("v1", "Verger Sfax Nord", "Route de Tunis km 8, Sfax", 12.5, "EN_RECOLTE",
                        34.7406, 10.7603, "var1", "u-agri1", new double[]{38, 44, 51}),
                verger("v2", "Verger Sousse Hammam", "Hammam Sousse, Sousse", 8.0, "EN_ATTENTE",
                        35.8256, 10.6369, "var2", "u-agri2", new double[]{22, 27, 31}),
                verger("v3", "Verger Zaghouan", "Route de Zriba, Zaghouan", 15.0, "EN_RECOLTE",
                        36.4029, 10.1429, "var2", "u-agri2", new double[]{55, 60, 66}),
                verger("v4", "Verger Béja Ouest", "Route de Téboursouk, Béja", 6.5, "RECOLTE_TERMINEE",
                        36.7256, 9.1817, "var1", "u-agri1", new double[]{78, 84, 90})));

        seed("bennes", List.of(
                benne("b1", "Benne 1", 3000.0, "TUN-1001"),
                benne("b2", "Benne 2", 3000.0, "TUN-1002"),
                benne("b3", "Benne 3", 3000.0, "TUN-1003"),
                benne("b4", "Benne 4", 4000.0, "TUN-1004")));

        seed("tournees", List.of(
                tournee("t5", now.minusDays(12), now.minusDays(11), "TERMINEE", List.of("r1", "r2")),
                tournee("t6", now.minusDays(6), now.minusDays(5), "TERMINEE", List.of("r3")),
                tournee("t7", now.minusDays(1), null, "EN_COURS", List.of("r4")),
                tournee("t8", now.plusDays(3), null, "PLANIFIEE", List.of("r5"))));

        seed("recoltes", List.of(
                recolte("r1", "v1", "t5", now.minusDays(14), 5200.0, 5000.0, false, "TERMINEE"),
                recolte("r2", "v4", "t5", now.minusDays(13), 2600.0, 2500.0, false, "TERMINEE"),
                recolte("r3", "v3", "t6", now.minusDays(9), 6100.0, 6000.0, true, "TERMINEE"),
                recolte("r4", "v1", "t7", now.minusDays(3), 3000.0, 1200.0, false, "EN_COURS"),
                recolte("r5", "v2", "t8", now.plusDays(2), 2000.0, null, false, "VALIDEE"),
                recolte("r6", "v2", null, now.plusDays(8), 1500.0, null, false, "EN_ATTENTE"),
                recolte("r7", "v3", null, now.minusDays(20), 900.0, null, false, "ANNULEE")));
    }

    private Document verger(String id, String nom, String adresse, double superficie, String statut,
                            double lat, double lng, String varieteId, String agriculteurId, double[] maturites) {
        List<Document> releves = new ArrayList<>();
        for (int i = 0; i < maturites.length; i++) {
            releves.add(new Document("date", date(now.minusDays((long) (maturites.length - i) * 3)))
                    .append("niveauMaturite", maturites[i]));
        }
        return new Document("_id", id)
                .append("nom", nom).append("adresse", adresse).append("superficie", superficie)
                .append("statut", statut).append("agriculteurId", agriculteurId)
                .append("location", new Document("type", "Point").append("coordinates", List.of(lng, lat)))
                .append("varieteDominanteId", varieteId)
                .append("parcelles", List.of(
                        new Document("id", id + "-p1").append("nom", "Parcelle A").append("superficie", superficie * 0.6)
                                .append("varieteId", varieteId),
                        new Document("id", id + "-p2").append("nom", "Parcelle B").append("superficie", superficie * 0.4)
                                .append("varieteId", varieteId)))
                .append("derniersReleves", releves);
    }

    private Document benne(String id, String nom, double capacite, String immatriculation) {
        return new Document("_id", id).append("nom", nom).append("disponibilite", true)
                .append("capacite", capacite).append("immatriculation", immatriculation);
    }

    private Document tournee(String id, LocalDateTime debut, LocalDateTime fin, String statut, List<String> recolteIds) {
        Document d = new Document("_id", id).append("dateDebut", date(debut)).append("statut", statut)
                .append("responsableCollecteId", "u-collecte").append("chefEquipeId", "u-chef")
                .append("recolteIds", recolteIds);
        if (fin != null) {
            d.append("dateFin", date(fin));
        }
        return d;
    }

    private Document recolte(String id, String vergerId, String tourneeId, LocalDateTime souhaitee,
                             Double estimee, Double recoltee, boolean urgente, String statut) {
        Document d = new Document("_id", id).append("vergerId", vergerId)
                .append("dateSouhaitee", date(souhaitee)).append("quantiteEstimee", estimee)
                .append("urgente", urgente).append("statut", statut);
        if (tourneeId != null) {
            d.append("tourneeId", tourneeId);
        }
        if (recoltee != null) {
            d.append("quantiteRecoltee", recoltee);
        }
        return d;
    }

    // ------------------------------------------------------------------ données M4

    private void seedDonneesM4() {
        seed("pressoirs", List.of(
                new Document("_id", "p1").append("nom", "Pressoir Moderne de Sfax")
                        .append("adresse", "Zone industrielle Poudrière, Sfax")
                        .append("capaciteTraitement", 20000.0).append("responsableId", "u-pressoir"),
                new Document("_id", "p2").append("nom", "Coopérative Oléicole de Zaghouan")
                        .append("adresse", "Route de Tunis, Zaghouan")
                        .append("capaciteTraitement", 12000.0).append("responsableId", "u-pressoir2")));

        seed("receptions", List.of(
                reception("rc1", "t5", "b1", "p1", now.minusDays(11), 2900.0, "BONNE"),
                reception("rc2", "t5", "b2", "p1", now.minusDays(11), 2950.0, "EXCELLENTE"),
                reception("rc3", "t5", "b3", "p1", now.minusDays(11), 1450.0, "MOYENNE"),
                reception("rc4", "t6", "b1", "p2", now.minusDays(5), 2400.0, "BONNE"),
                reception("rc5", "t6", "b2", "p2", now.minusDays(5), 2300.0, "EXCELLENTE"),
                reception("rc6", "t6", "b3", "p2", now.minusDays(5), 1100.0, "BONNE"),
                reception("rc7", "t7", "b1", "p1", now.minusHours(6), 1150.0, "BONNE")));

        List<Document> releves = new ArrayList<>();
        List<Document> meteo = new ArrayList<>();
        String[] vergers = {"v1", "v2", "v3", "v4"};
        double[][] maturites = {{38, 44, 51}, {22, 27, 31}, {55, 60, 66}, {78, 84, 90}};
        for (int v = 0; v < vergers.length; v++) {
            for (int i = 0; i < 3; i++) {
                LocalDateTime t = now.minusDays((long) (3 - i) * 3);
                releves.add(new Document("vergerId", vergers[v]).append("dateReleve", date(t))
                        .append("niveauMaturite", maturites[v][i]));
                meteo.add(new Document("vergerId", vergers[v]).append("dateReleve", date(t))
                        .append("temperature", 17.0 + v + i).append("humidite", 55.0 + 3 * i)
                        .append("precipitation", i == 1 ? 2.4 : 0.0));
            }
        }
        seed("releves_maturite", releves);
        seed("meteo", meteo);
    }

    private Document reception(String id, String tourneeId, String benneId, String pressoirId,
                               LocalDateTime date, double poids, String qualite) {
        return new Document("_id", id).append("tourneeId", tourneeId).append("benneId", benneId)
                .append("pressoirId", pressoirId).append("dateReception", date(date))
                .append("poids", poids).append("qualite", qualite);
    }

    // ------------------------------------------------------------------ utilitaires

    private void seed(String collection, List<Document> docs) {
        if (mongo.getCollection(collection).countDocuments() == 0) {
            mongo.getCollection(collection).insertMany(docs);
            log.info("Seed dev : {} document(s) insérés dans '{}'", docs.size(), collection);
        } else {
            log.info("Seed dev : '{}' déjà peuplée, ignorée", collection);
        }
    }

    private static Date date(LocalDateTime t) {
        return Date.from(t.atZone(ZoneId.systemDefault()).toInstant());
    }
}
