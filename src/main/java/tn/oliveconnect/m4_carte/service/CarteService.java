package tn.oliveconnect.m4_carte.service;

import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import tn.oliveconnect.m4_carte.dto.CartePubliqueVergerResponse;
import tn.oliveconnect.m4_carte.dto.CarteVergerResponse;
import tn.oliveconnect.m4_carte.dto.MeteoRecenteResponse;
import tn.oliveconnect.m4_carte.dto.ReleveRecentResponse;
import tn.oliveconnect.m4_carte.repository.CarteRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Carte interactive (F4). Les deux versions sont mises en cache (TTL : oliveconnect.carte.cache-ttl-ms)
 * pour tenir la charge (~100 connexions) et l'objectif "carte en moins de 10 s".
 */
@Service
@RequiredArgsConstructor
public class CarteService {

    public static final String CACHE_PUBLIQUE = "cartePublique";
    public static final String CACHE_COMPLETE = "carteComplete";

    private final CarteRepository carteRepository;

    /** Version publique (Visiteur) : position, maturité, type d'olives. */
    @Cacheable(CACHE_PUBLIQUE)
    public List<CartePubliqueVergerResponse> carteVergersPublique() {
        Map<String, String> varietes = carteRepository.findNomsVarietes();
        List<CartePubliqueVergerResponse> res = new ArrayList<>();
        for (Document v : carteRepository.findVergers()) {
            double[] position = position(v);
            if (position == null) {
                continue;
            }
            ReleveRecentResponse dernier = Docs.dernierReleve(v);
            res.add(new CartePubliqueVergerResponse(
                    Docs.id(v), Docs.str(v, "nom"), position[0], position[1],
                    dernier == null ? null : dernier.niveauMaturite(),
                    typeOlives(v, varietes)));
        }
        return res;
    }

    /** Version complète (profils connectés) : statuts, relevés récents, météo récente. */
    @Cacheable(CACHE_COMPLETE)
    public List<CarteVergerResponse> carteVergersComplete() {
        Map<String, String> varietes = carteRepository.findNomsVarietes();
        Map<String, String> statutsRecoltes = carteRepository.dernierStatutRecoltes();
        Map<String, Document> meteos = carteRepository.dernieresMeteo();
        List<CarteVergerResponse> res = new ArrayList<>();
        for (Document v : carteRepository.findVergers()) {
            double[] position = position(v);
            if (position == null) {
                continue;
            }
            String id = Docs.id(v);
            List<ReleveRecentResponse> releves = Docs.derniersReleves(v);
            ReleveRecentResponse dernier = releves.isEmpty() ? null : releves.get(releves.size() - 1);
            Document m = meteos.get(id);
            MeteoRecenteResponse meteo = m == null ? null : new MeteoRecenteResponse(
                    Docs.ldt(m.get("dateReleve")), Docs.num(m, "temperature"),
                    Docs.num(m, "humidite"), Docs.num(m, "precipitation"));
            res.add(new CarteVergerResponse(
                    id, Docs.str(v, "nom"), Docs.str(v, "adresse"), Docs.num(v, "superficie"),
                    position[0], position[1],
                    Docs.str(v, "statut"), statutsRecoltes.get(id),
                    typeOlives(v, varietes),
                    dernier == null ? null : dernier.niveauMaturite(),
                    dernier == null ? null : dernier.date(),
                    releves, meteo));
        }
        return res;
    }

    /** {latitude, longitude} depuis le GeoJSON Point {type, coordinates:[lng, lat]}, ou null si invalide. */
    private static double[] position(Document verger) {
        Object loc = verger.get("location");
        if (!(loc instanceof Document location)) {
            return null;
        }
        Object coords = location.get("coordinates");
        if (!(coords instanceof List<?> list) || list.size() < 2
                || !(list.get(0) instanceof Number lng) || !(list.get(1) instanceof Number lat)) {
            return null;
        }
        double latitude = lat.doubleValue();
        double longitude = lng.doubleValue();
        if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
            return null;
        }
        return new double[]{latitude, longitude};
    }

    /** Variété dominante du verger, à défaut la variété de sa première parcelle. */
    private static String typeOlives(Document verger, Map<String, String> varietes) {
        Object dominante = verger.get("varieteDominanteId");
        if (dominante != null && varietes.get(dominante.toString()) != null) {
            return varietes.get(dominante.toString());
        }
        for (Document parcelle : Docs.documents(verger, "parcelles")) {
            String varieteId = Docs.str(parcelle, "varieteId");
            if (varieteId != null && varietes.get(varieteId) != null) {
                return varietes.get(varieteId);
            }
        }
        return null;
    }
}
