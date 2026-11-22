package tn.oliveconnect.m4_carte.repository;

import com.mongodb.client.model.Projections;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lectures (seule lecture, aucune écriture) nécessaires à la carte : vergers et variétés (M1),
 * statut de la dernière récolte (M1) et dernière météo (M4).
 * Les ids sont renvoyés sous forme de chaînes, quelle que soit leur représentation en base.
 */
@Repository
public class CarteRepository {

    private final MongoTemplate mongoTemplate;

    public CarteRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public List<Document> findVergers() {
        return mongoTemplate.getCollection("vergers")
                .find()
                .projection(Projections.include("nom", "adresse", "superficie", "statut", "location",
                        "varieteDominanteId", "parcelles", "derniersReleves"))
                .into(new ArrayList<>());
    }

    /** id de variété -> nom. */
    public Map<String, String> findNomsVarietes() {
        Map<String, String> noms = new HashMap<>();
        for (Document d : mongoTemplate.getCollection("varietes").find().projection(Projections.include("nom"))) {
            Object nom = d.get("nom");
            noms.put(String.valueOf(d.get("_id")), nom == null ? null : nom.toString());
        }
        return noms;
    }

    /** id de verger -> statut de sa récolte la plus récente (par dateSouhaitee), hors récoltes annulées. */
    public Map<String, String> dernierStatutRecoltes() {
        List<Document> pipeline = List.of(
                Document.parse("""
                        {"$match": {"statut": {"$ne": "ANNULEE"}, "vergerId": {"$ne": null}}}
                        """),
                Document.parse("""
                        {"$sort": {"dateSouhaitee": -1}}
                        """),
                Document.parse("""
                        {"$group": {"_id": {"$toString": "$vergerId"}, "statut": {"$first": "$statut"}}}
                        """));
        Map<String, String> res = new HashMap<>();
        for (Document d : mongoTemplate.getCollection("recoltes").aggregate(pipeline).allowDiskUse(true)) {
            Object statut = d.get("statut");
            res.put(String.valueOf(d.get("_id")), statut == null ? null : statut.toString());
        }
        return res;
    }

    /** id de verger -> dernier relevé météo {dateReleve, temperature, humidite, precipitation}. */
    public Map<String, Document> dernieresMeteo() {
        List<Document> pipeline = List.of(
                Document.parse("""
                        {"$sort": {"vergerId": 1, "dateReleve": -1}}
                        """),
                Document.parse("""
                        {"$group": {"_id": "$vergerId",
                                    "dateReleve": {"$first": "$dateReleve"},
                                    "temperature": {"$first": "$temperature"},
                                    "humidite": {"$first": "$humidite"},
                                    "precipitation": {"$first": "$precipitation"}}}
                        """));
        Map<String, Document> res = new HashMap<>();
        for (Document d : mongoTemplate.getCollection("meteo").aggregate(pipeline).allowDiskUse(true)) {
            res.put(String.valueOf(d.get("_id")), d);
        }
        return res;
    }
}
