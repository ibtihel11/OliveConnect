package tn.oliveconnect.m4_carte.repository;

import com.mongodb.client.model.CountOptions;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;
import tn.oliveconnect.m4_carte.integration.MongoIds;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * Seule écriture de M4 dans la collection "vergers" de M1 : le tableau imbriqué derniersReleves
 * ({date, niveauMaturite}, 5 derniers, cadrage 4.1). Aucun autre champ du verger n'est touché.
 */
@Repository
public class VergerReleveRepository {

    public static final int NB_RELEVES_IMBRIQUES = 5;

    private final MongoTemplate mongoTemplate;

    public VergerReleveRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public boolean existe(String vergerId) {
        return mongoTemplate.getCollection("vergers")
                .countDocuments(MongoIds.filtre(vergerId), new CountOptions().limit(1)) > 0;
    }

    /** Ajoute le relevé, trie par date et ne conserve que les 5 plus récents. */
    public boolean pousserDernierReleve(String vergerId, LocalDateTime date, Double niveauMaturite) {
        Document releve = new Document("date", Date.from(date.atZone(ZoneId.systemDefault()).toInstant()))
                .append("niveauMaturite", niveauMaturite);
        Document update = new Document("$push", new Document("derniersReleves",
                new Document("$each", List.of(releve))
                        .append("$sort", new Document("date", 1))
                        .append("$slice", -NB_RELEVES_IMBRIQUES)));
        UpdateResult r = mongoTemplate.getCollection("vergers").updateOne(MongoIds.filtre(vergerId), update);
        return r.getMatchedCount() > 0;
    }
}
