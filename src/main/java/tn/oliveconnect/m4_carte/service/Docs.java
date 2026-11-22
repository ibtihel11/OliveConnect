package tn.oliveconnect.m4_carte.service;

import org.bson.Document;
import tn.oliveconnect.m4_carte.dto.ReleveRecentResponse;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/** Lecture tolérante des documents MongoDB renvoyés par les agrégations (types numériques et dates variables). */
final class Docs {

    private Docs() {
    }

    static String id(Document d) {
        Object v = d.get("_id");
        return v == null ? null : v.toString();
    }

    static String str(Document d, String key) {
        Object v = d.get(key);
        return v == null ? null : v.toString();
    }

    static Double num(Document d, String key) {
        Object v = d.get(key);
        return v instanceof Number n ? n.doubleValue() : null;
    }

    static long lng(Document d, String key) {
        Object v = d.get(key);
        return v instanceof Number n ? n.longValue() : 0L;
    }

    static Double round2(Double v) {
        if (v == null || v.isNaN() || v.isInfinite()) {
            return null;
        }
        return Math.round(v * 100.0) / 100.0;
    }

    static LocalDateTime ldt(Object v) {
        if (v instanceof Date date) {
            return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
        }
        if (v instanceof LocalDateTime l) {
            return l;
        }
        if (v instanceof Instant i) {
            return LocalDateTime.ofInstant(i, ZoneId.systemDefault());
        }
        if (v instanceof String s) {
            try {
                return LocalDateTime.parse(s);
            } catch (DateTimeParseException e) {
                return null;
            }
        }
        return null;
    }

    static List<Document> documents(Document d, String key) {
        Object v = d.get(key);
        List<Document> res = new ArrayList<>();
        if (v instanceof List<?> list) {
            for (Object o : list) {
                if (o instanceof Document sub) {
                    res.add(sub);
                }
            }
        }
        return res;
    }

    /** Relevés imbriqués d'un verger ({date, niveauMaturite}), triés par date croissante. */
    static List<ReleveRecentResponse> derniersReleves(Document verger) {
        List<ReleveRecentResponse> res = new ArrayList<>();
        for (Document r : documents(verger, "derniersReleves")) {
            Object date = r.get("date") != null ? r.get("date") : r.get("dateReleve");
            res.add(new ReleveRecentResponse(ldt(date), num(r, "niveauMaturite")));
        }
        res.sort(Comparator.comparing(ReleveRecentResponse::date, Comparator.nullsFirst(Comparator.naturalOrder())));
        return res;
    }

    /** Dernier relevé (le plus récent) d'un verger, ou null. */
    static ReleveRecentResponse dernierReleve(Document verger) {
        List<ReleveRecentResponse> releves = derniersReleves(verger);
        return releves.isEmpty() ? null : releves.get(releves.size() - 1);
    }
}
