package tn.oliveconnect.m4_carte.integration;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class MongoTourneeReader implements TourneeReader {

    private final MongoTemplate mongoTemplate;

    public MongoTourneeReader(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Optional<TourneeRef> findById(String id) {
        Document d = mongoTemplate.getCollection("tournees")
                .find(MongoIds.filtre(id))
                .projection(new Document("statut", 1))
                .first();
        if (d == null) {
            return Optional.empty();
        }
        Object statut = d.get("statut");
        return Optional.of(new TourneeRef(String.valueOf(d.get("_id")), statut == null ? null : statut.toString()));
    }
}
