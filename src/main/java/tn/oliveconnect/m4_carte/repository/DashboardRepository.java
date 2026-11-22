package tn.oliveconnect.m4_carte.repository;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * Pipelines d'agrégation du tableau de bord (cadrage règle 10 : $group, $lookup, $match ; aucune collection créée).
 * Lecture seule sur receptions (M4), recoltes / tournees / vergers (M1) et bennes (M3).
 *
 * Les jointures comparent les ids sous forme de chaîne ($toString) : elles fonctionnent que _id soit
 * un ObjectId (ids générés) ou une chaîne (ids saisis), et que les références soient stockées en chaîne.
 */
@Repository
public class DashboardRepository {

    private final MongoTemplate mongoTemplate;

    public DashboardRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Rendement par verger. Le poids réceptionné d'une tournée est réparti entre ses récoltes (non annulées)
     * au prorata de quantiteRecoltee (à parts égales si aucune quantité n'est encore saisie).
     * Champs : vergerId, nomVerger, superficie, quantiteRecoltee, poidsRecu, nombreTournees,
     * rendementKgParHa, tauxReception.
     */
    public List<Document> rendementParVerger() {
        List<Document> pipeline = List.of(
                Document.parse("""
                        {"$group": {"_id": "$tourneeId", "poidsRecu": {"$sum": "$poids"}}}
                        """),
                Document.parse("""
                        {"$match": {"_id": {"$ne": null}}}
                        """),
                Document.parse("""
                        {"$lookup": {"from": "recoltes", "let": {"tid": "$_id"}, "pipeline": [
                            {"$match": {"$expr": {"$and": [
                                {"$eq": [{"$toString": "$tourneeId"}, "$$tid"]},
                                {"$ne": ["$statut", "ANNULEE"]}
                            ]}}},
                            {"$project": {"vergerId": {"$toString": "$vergerId"},
                                          "quantiteRecoltee": {"$ifNull": ["$quantiteRecoltee", 0]}}}
                        ], "as": "recoltes"}}
                        """),
                Document.parse("""
                        {"$addFields": {"totalRecolte": {"$sum": "$recoltes.quantiteRecoltee"},
                                        "nbRecoltes": {"$size": "$recoltes"}}}
                        """),
                Document.parse("""
                        {"$unwind": "$recoltes"}
                        """),
                Document.parse("""
                        {"$addFields": {"poidsAttribue": {"$cond": [
                            {"$gt": ["$totalRecolte", 0]},
                            {"$multiply": ["$poidsRecu", {"$divide": ["$recoltes.quantiteRecoltee", "$totalRecolte"]}]},
                            {"$divide": ["$poidsRecu", "$nbRecoltes"]}
                        ]}}}
                        """),
                Document.parse("""
                        {"$group": {"_id": "$recoltes.vergerId",
                                    "poidsRecu": {"$sum": "$poidsAttribue"},
                                    "quantiteRecoltee": {"$sum": "$recoltes.quantiteRecoltee"},
                                    "tournees": {"$addToSet": "$_id"}}}
                        """),
                lookupParId("vergers", "$_id", "{\"nom\": 1, \"superficie\": 1}", "verger"),
                Document.parse("""
                        {"$unwind": {"path": "$verger", "preserveNullAndEmptyArrays": true}}
                        """),
                Document.parse("""
                        {"$project": {"_id": 0,
                                      "vergerId": "$_id",
                                      "nomVerger": "$verger.nom",
                                      "superficie": "$verger.superficie",
                                      "quantiteRecoltee": 1,
                                      "poidsRecu": 1,
                                      "nombreTournees": {"$size": "$tournees"},
                                      "rendementKgParHa": {"$cond": [
                                          {"$gt": ["$verger.superficie", 0]},
                                          {"$divide": ["$poidsRecu", "$verger.superficie"]},
                                          null]},
                                      "tauxReception": {"$cond": [
                                          {"$gt": ["$quantiteRecoltee", 0]},
                                          {"$multiply": [{"$divide": ["$poidsRecu", "$quantiteRecoltee"]}, 100]},
                                          null]}}}
                        """),
                Document.parse("""
                        {"$sort": {"rendementKgParHa": -1}}
                        """));
        return run("receptions", pipeline);
    }

    /**
     * Délais de collecte : (début de tournée - dateSouhaitee) en jours, pour les récoltes non annulées
     * rattachées à une tournée EN_COURS ou TERMINEE. Retourne un document unique
     * {global: [...], parType: [...], parVerger: [...]} (résultat d'un $facet).
     */
    public Document delaisCollecte() {
        Document facet = new Document("$facet", new Document()
                .append("global", List.of(Document.parse("""
                        {"$group": {"_id": null,
                                    "moyenne": {"$avg": "$delaiJours"},
                                    "min": {"$min": "$delaiJours"},
                                    "max": {"$max": "$delaiJours"},
                                    "n": {"$sum": 1}}}
                        """)))
                .append("parType", List.of(
                        Document.parse("""
                                {"$group": {"_id": "$urgente", "moyenne": {"$avg": "$delaiJours"}, "n": {"$sum": 1}}}
                                """),
                        Document.parse("""
                                {"$sort": {"_id": 1}}
                                """)))
                .append("parVerger", List.of(
                        Document.parse("""
                                {"$group": {"_id": "$vergerId", "moyenne": {"$avg": "$delaiJours"}, "n": {"$sum": 1}}}
                                """),
                        lookupParId("vergers", "$_id", "{\"nom\": 1}", "verger"),
                        Document.parse("""
                                {"$unwind": {"path": "$verger", "preserveNullAndEmptyArrays": true}}
                                """),
                        Document.parse("""
                                {"$project": {"_id": 0, "vergerId": "$_id", "nomVerger": "$verger.nom",
                                              "moyenne": 1, "n": 1}}
                                """),
                        Document.parse("""
                                {"$sort": {"moyenne": -1}}
                                """))));

        List<Document> pipeline = List.of(
                Document.parse("""
                        {"$match": {"tourneeId": {"$ne": null}, "statut": {"$ne": "ANNULEE"}}}
                        """),
                Document.parse("""
                        {"$lookup": {"from": "tournees", "let": {"tid": {"$toString": "$tourneeId"}}, "pipeline": [
                            {"$match": {"$expr": {"$and": [
                                {"$eq": [{"$toString": "$_id"}, "$$tid"]},
                                {"$in": ["$statut", ["EN_COURS", "TERMINEE"]]}
                            ]}}},
                            {"$project": {"dateDebut": 1}}
                        ], "as": "tournee"}}
                        """),
                Document.parse("""
                        {"$unwind": "$tournee"}
                        """),
                Document.parse("""
                        {"$project": {"vergerId": {"$toString": "$vergerId"},
                                      "urgente": {"$ifNull": ["$urgente", false]},
                                      "delaiJours": {"$divide": [
                                          {"$subtract": [{"$toDate": "$tournee.dateDebut"}, {"$toDate": "$dateSouhaitee"}]},
                                          86400000]}}}
                        """),
                Document.parse("""
                        {"$match": {"delaiJours": {"$ne": null}}}
                        """),
                facet);
        List<Document> res = run("recoltes", pipeline);
        return res.isEmpty() ? new Document() : res.get(0);
    }

    /**
     * Remplissage des bennes, une ligne par benne :
     * benneId, immatriculation, capaciteKg, nombreReceptions, poidsMoyenKg,
     * tauxRemplissageMoyen, tauxRemplissageMax (en %). Bennes inconnues ou de capacité nulle ignorées.
     */
    public List<Document> remplissageParBenne() {
        List<Document> pipeline = List.of(
                Document.parse("""
                        {"$group": {"_id": "$benneId", "n": {"$sum": 1},
                                    "poidsTotal": {"$sum": "$poids"}, "poidsMax": {"$max": "$poids"}}}
                        """),
                Document.parse("""
                        {"$match": {"_id": {"$ne": null}}}
                        """),
                lookupParId("bennes", "$_id", "{\"immatriculation\": 1, \"capacite\": 1}", "benne"),
                Document.parse("""
                        {"$unwind": "$benne"}
                        """),
                Document.parse("""
                        {"$match": {"benne.capacite": {"$gt": 0}}}
                        """),
                Document.parse("""
                        {"$project": {"_id": 0,
                                      "benneId": "$_id",
                                      "immatriculation": "$benne.immatriculation",
                                      "capaciteKg": "$benne.capacite",
                                      "nombreReceptions": "$n",
                                      "poidsMoyenKg": {"$divide": ["$poidsTotal", "$n"]},
                                      "tauxRemplissageMoyen": {"$multiply": [
                                          {"$divide": ["$poidsTotal", {"$multiply": ["$n", "$benne.capacite"]}]}, 100]},
                                      "tauxRemplissageMax": {"$multiply": [
                                          {"$divide": ["$poidsMax", "$benne.capacite"]}, 100]}}}
                        """),
                Document.parse("""
                        {"$sort": {"tauxRemplissageMoyen": -1}}
                        """));
        return run("receptions", pipeline);
    }

    /** $lookup par id comparé en chaîne : fonctionne pour _id ObjectId comme pour _id String. */
    private static Document lookupParId(String from, String localExpr, String projectJson, String as) {
        return new Document("$lookup", new Document("from", from)
                .append("let", new Document("k", localExpr))
                .append("pipeline", List.of(
                        new Document("$match", new Document("$expr",
                                new Document("$eq", List.of(new Document("$toString", "$_id"), "$$k")))),
                        new Document("$project", Document.parse(projectJson))))
                .append("as", as));
    }

    private List<Document> run(String collection, List<Document> pipeline) {
        return mongoTemplate.getCollection(collection)
                .aggregate(pipeline)
                .allowDiskUse(true)
                .into(new ArrayList<>());
    }
}
