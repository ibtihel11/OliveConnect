package tn.oliveconnect.m4_carte.integration;

import com.mongodb.client.model.Filters;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

/**
 * Les ids sont des chaînes dans l'API ; en base, Spring Data stocke _id en ObjectId (ids générés par M1/M3)
 * ou en chaîne (ids saisis, ex. "t7"). Ce filtre accepte les deux représentations.
 */
public final class MongoIds {

    private MongoIds() {
    }

    public static Bson filtre(String id) {
        if (id != null && ObjectId.isValid(id)) {
            return Filters.or(Filters.eq("_id", id), Filters.eq("_id", new ObjectId(id)));
        }
        return Filters.eq("_id", id);
    }
}
