package tn.oliveconnect.m4_carte.integration;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class MongoBenneReader implements BenneReader {

    private final MongoTemplate mongoTemplate;

    public MongoBenneReader(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Optional<BenneRef> findById(String id) {
        Document d = mongoTemplate.getCollection("bennes")
                .find(MongoIds.filtre(id))
                .projection(new Document("immatriculation", 1).append("capacite", 1))
                .first();
        if (d == null) {
            return Optional.empty();
        }
        Object immat = d.get("immatriculation");
        Object cap = d.get("capacite");
        return Optional.of(new BenneRef(String.valueOf(d.get("_id")),
                immat == null ? null : immat.toString(),
                cap instanceof Number n ? n.doubleValue() : null));
    }
}
