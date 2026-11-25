package tn.oliveconnect.m3_ressources.service;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tn.oliveconnect.common.exception.BadRequestException;
import tn.oliveconnect.m3_ressources.dto.BenneRequest;
import tn.oliveconnect.m3_ressources.dto.ImportErreurResponse;
import tn.oliveconnect.m3_ressources.dto.ImportRessourcesResponse;
import tn.oliveconnect.m3_ressources.dto.MaterielRequest;
import tn.oliveconnect.m3_ressources.dto.OuvrierRequest;
import tn.oliveconnect.m3_ressources.dto.TracteurRequest;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Import explicite de fichiers M3. Aucune source ni exécution périodique de fichiers n'est configurée. */
@Service
@RequiredArgsConstructor
public class ImportRessourcesService {

    private static final String TYPE_COLUMN = "ressourceType";
    private static final Map<String, List<String>> CHAMPS_PAR_TYPE = Map.of(
            "ouvrier", List.of("nom", "disponibilite", "specialite", "saisonnier"),
            "tracteur", List.of("nom", "disponibilite", "immatriculation", "puissance"),
            "benne", List.of("nom", "disponibilite", "immatriculation", "capacite"),
            "materiel", List.of("nom", "disponibilite", "type", "quantiteStock", "seuilMinimum"));

    private final ObjectMapper objectMapper;
    private final Validator validator;
    private final OuvrierService ouvrierService;
    private final TracteurService tracteurService;
    private final BenneService benneService;
    private final MaterielService materielService;

    public ImportRessourcesResponse importer(MultipartFile fichier) {
        if (fichier == null || fichier.isEmpty()) {
            throw new BadRequestException("Le fichier d'import est vide");
        }
        final String contenu;
        try {
            contenu = new String(fichier.getBytes(), StandardCharsets.UTF_8).replace("\uFEFF", "");
        } catch (IOException e) {
            throw new BadRequestException("Impossible de lire le fichier d'import");
        }
        if (contenu.isBlank()) {
            throw new BadRequestException("Le fichier d'import est vide");
        }

        Format format = detecterFormat(fichier.getOriginalFilename(), contenu);
        List<LigneImport> lignes = format == Format.JSON ? lireJson(contenu) : lireCsv(contenu);
        List<ImportErreurResponse> erreurs = new ArrayList<>();
        ContexteImport contexte = new ContexteImport();
        int importes = 0;
        for (LigneImport ligne : lignes) {
            try {
                enregistrer(ligne, contexte);
                importes++;
            } catch (LigneInvalideException | DuplicateKeyException e) {
                erreurs.add(new ImportErreurResponse(ligne.numero(), e.getMessage()));
            }
        }
        return new ImportRessourcesResponse(lignes.size(), importes, erreurs.size(), List.copyOf(erreurs));
    }

    private Format detecterFormat(String nom, String contenu) {
        if (nom != null) {
            String lower = nom.toLowerCase(Locale.ROOT);
            if (lower.endsWith(".json")) return Format.JSON;
            if (lower.endsWith(".csv")) return Format.CSV;
        }
        String debut = contenu.stripLeading();
        if (debut.startsWith("[") || debut.startsWith("{")) return Format.JSON;
        if (debut.contains(",") || debut.contains("\n") || debut.contains("\r")) return Format.CSV;
        throw new BadRequestException("Format de fichier non pris en charge : utiliser JSON ou CSV");
    }

    private List<LigneImport> lireJson(String contenu) {
        try {
            JsonNode racine = objectMapper.readTree(contenu);
            if (racine == null || !racine.isArray()) {
                throw new BadRequestException("Le fichier JSON doit contenir un tableau d'objets ressources");
            }
            List<LigneImport> lignes = new ArrayList<>();
            int numero = 0;
            for (JsonNode item : racine) {
                numero++;
                if (!item.isObject()) {
                    lignes.add(new LigneImport(numero, Map.of(), null, "Chaque élément JSON doit être un objet"));
                    continue;
                }
                Map<String, Object> valeurs = new LinkedHashMap<>();
                item.properties().forEach(field -> valeurs.put(field.getKey(), valeurJson(field.getValue())));
                String type = valeurs.containsKey(TYPE_COLUMN) ? texte(valeurs.remove(TYPE_COLUMN)) : null;
                lignes.add(new LigneImport(numero, valeurs, type, null));
            }
            return lignes;
        } catch (JacksonException e) {
            throw new BadRequestException("Fichier JSON illisible ou mal formé");
        }
    }

    private Object valeurJson(JsonNode valeur) {
        if (valeur == null || valeur.isNull()) return null;
        if (valeur.isTextual()) return valeur.textValue();
        if (valeur.isBoolean()) return valeur.booleanValue();
        if (valeur.isNumber()) return valeur.numberValue();
        if (valeur.isArray() || valeur.isObject()) return valeur.toString();
        return null;
    }

    private List<LigneImport> lireCsv(String contenu) {
        List<EnregistrementCsv> records = parserCsv(contenu);
        if (records.isEmpty()) throw new BadRequestException("Le fichier CSV ne contient pas d'en-tête");
        List<String> entetes = records.get(0).valeurs().stream().map(String::trim).toList();
        if (entetes.stream().anyMatch(String::isBlank) || new HashSet<>(entetes).size() != entetes.size()) {
            throw new BadRequestException("L'en-tête CSV contient un nom vide ou dupliqué");
        }
        Set<String> autorises = new HashSet<>(CHAMPS_PAR_TYPE.values().stream().flatMap(List::stream).toList());
        autorises.add(TYPE_COLUMN);
        String inconnu = entetes.stream().filter(champ -> !autorises.contains(champ)).findFirst().orElse(null);
        if (inconnu != null) throw new BadRequestException("Colonne CSV non reconnue : " + inconnu);

        List<LigneImport> lignes = new ArrayList<>();
        for (int i = 1; i < records.size(); i++) {
            EnregistrementCsv record = records.get(i);
            if (record.valeurs().size() == 1 && record.valeurs().get(0).isBlank()) continue;
            if (record.valeurs().size() > entetes.size()) {
                lignes.add(new LigneImport(record.numero(), Map.of(), null,
                        "Nombre de colonnes supérieur à celui de l'en-tête"));
                continue;
            }
            Map<String, Object> valeurs = new LinkedHashMap<>();
            for (int col = 0; col < entetes.size(); col++) {
                String value = col < record.valeurs().size() ? record.valeurs().get(col).trim() : "";
                valeurs.put(entetes.get(col), value);
            }
            String type = texte(valeurs.remove(TYPE_COLUMN));
            valeurs.values().removeIf(value -> value instanceof String s && s.isEmpty());
            lignes.add(new LigneImport(record.numero(), valeurs, type, null));
        }
        return lignes;
    }

    /** CSV RFC-4180 minimal : guillemets échappés, virgules et retours de ligne dans les champs cités. */
    private List<EnregistrementCsv> parserCsv(String texte) {
        List<EnregistrementCsv> records = new ArrayList<>();
        List<String> champs = new ArrayList<>();
        StringBuilder champ = new StringBuilder();
        boolean cite = false;
        int lignePhysique = 1;
        int debutRecord = 1;
        for (int i = 0; i < texte.length(); i++) {
            char c = texte.charAt(i);
            if (c == '"') {
                if (cite && i + 1 < texte.length() && texte.charAt(i + 1) == '"') {
                    champ.append('"');
                    i++;
                } else {
                    cite = !cite;
                }
            } else if (c == ',' && !cite) {
                champs.add(champ.toString());
                champ.setLength(0);
            } else if ((c == '\n' || c == '\r') && !cite) {
                champs.add(champ.toString());
                champ.setLength(0);
                records.add(new EnregistrementCsv(debutRecord, List.copyOf(champs)));
                champs.clear();
                if (c == '\r' && i + 1 < texte.length() && texte.charAt(i + 1) == '\n') i++;
                lignePhysique++;
                debutRecord = lignePhysique;
            } else {
                champ.append(c);
                if (c == '\n') lignePhysique++;
            }
        }
        if (cite) throw new BadRequestException("Fichier CSV invalide : guillemet non fermé");
        if (champ.length() > 0 || !champs.isEmpty()) {
            champs.add(champ.toString());
            records.add(new EnregistrementCsv(debutRecord, List.copyOf(champs)));
        }
        return records;
    }

    private void enregistrer(LigneImport ligne, ContexteImport contexte) {
        if (ligne.erreur() != null) throw new LigneInvalideException(ligne.erreur());
        String type = determinerType(ligne.type(), ligne.champs());
        List<String> champsAttendus = CHAMPS_PAR_TYPE.get(type);
        Set<String> inconnus = new HashSet<>(ligne.champs().keySet());
        inconnus.removeAll(champsAttendus);
        if (!inconnus.isEmpty()) {
            throw new LigneInvalideException("Champ(s) non reconnu(s) pour " + type + " : " + String.join(", ", inconnus));
        }
        Object request = construireRequest(type, ligne.champs());
        List<String> violations = validator.validate(request).stream()
                .map(ConstraintViolation::getMessage).sorted().toList();
        if (!violations.isEmpty()) throw new LigneInvalideException(String.join("; ", violations));
        try {
            switch (type) {
                case "ouvrier" -> ouvrierService.creer((OuvrierRequest) request);
                case "tracteur" -> importerTracteur((TracteurRequest) request, contexte);
                case "benne" -> importerBenne((BenneRequest) request, contexte);
                case "materiel" -> materielService.creer((MaterielRequest) request);
                default -> throw new LigneInvalideException("Type de ressource non pris en charge : " + type);
            }
        } catch (DuplicateKeyException e) {
            throw new LigneInvalideException("Doublon rejeté par une contrainte d'unicité de la base de données");
        }
    }

    private void importerTracteur(TracteurRequest request, ContexteImport contexte) {
        String immatriculation = ImmatriculationNormalizer.nettoyer(request.immatriculation());
        String cle = ImmatriculationNormalizer.cle(immatriculation);
        if (contexte.immatriculationsTracteurs.contains(cle)) {
            throw new LigneInvalideException("Immatriculation répétée dans ce fichier pour un tracteur");
        }
        if (tracteurService.existeImmatriculation(immatriculation)) {
            throw new LigneInvalideException("Immatriculation déjà présente dans la collection tracteurs");
        }
        tracteurService.creer(new TracteurRequest(request.nom(), request.disponibilite(), immatriculation,
                request.puissance()));
        contexte.immatriculationsTracteurs.add(cle);
    }

    private void importerBenne(BenneRequest request, ContexteImport contexte) {
        String immatriculation = ImmatriculationNormalizer.nettoyer(request.immatriculation());
        String cle = ImmatriculationNormalizer.cle(immatriculation);
        if (contexte.immatriculationsBennes.contains(cle)) {
            throw new LigneInvalideException("Immatriculation répétée dans ce fichier pour une benne");
        }
        if (benneService.existeImmatriculation(immatriculation)) {
            throw new LigneInvalideException("Immatriculation déjà présente dans la collection bennes");
        }
        benneService.creer(new BenneRequest(request.nom(), request.disponibilite(), request.capacite(),
                immatriculation));
        contexte.immatriculationsBennes.add(cle);
    }

    private String determinerType(String typeForce, Map<String, Object> champs) {
        if (typeForce != null && !typeForce.isBlank()) {
            String type = typeForce.toLowerCase(Locale.ROOT);
            if (!CHAMPS_PAR_TYPE.containsKey(type)) {
                throw new LigneInvalideException("Type de ressource invalide : " + typeForce);
            }
            return type;
        }
        List<String> candidats = new ArrayList<>();
        if (champs.containsKey("specialite") || champs.containsKey("saisonnier")) candidats.add("ouvrier");
        if (champs.containsKey("puissance")) candidats.add("tracteur");
        if (champs.containsKey("capacite")) candidats.add("benne");
        if (champs.containsKey("quantiteStock") || champs.containsKey("seuilMinimum")) candidats.add("materiel");
        if (candidats.size() != 1) {
            throw new LigneInvalideException("Type de ressource absent ou ambigu; fournir ressourceType");
        }
        return candidats.get(0);
    }

    private Object construireRequest(String type, Map<String, Object> champs) {
        return switch (type) {
            case "ouvrier" -> new OuvrierRequest(chaine(champs, "nom"), booleen(champs, "disponibilite"),
                    chaine(champs, "specialite"), booleen(champs, "saisonnier"));
            case "tracteur" -> new TracteurRequest(chaine(champs, "nom"), booleen(champs, "disponibilite"),
                    ImmatriculationNormalizer.nettoyer(chaine(champs, "immatriculation")), decimal(champs, "puissance"));
            case "benne" -> new BenneRequest(chaine(champs, "nom"), booleen(champs, "disponibilite"),
                    decimal(champs, "capacite"), ImmatriculationNormalizer.nettoyer(chaine(champs, "immatriculation")));
            case "materiel" -> new MaterielRequest(chaine(champs, "nom"), booleen(champs, "disponibilite"),
                    chaine(champs, "type"), entier(champs, "quantiteStock"), entier(champs, "seuilMinimum"));
            default -> throw new LigneInvalideException("Type de ressource non pris en charge : " + type);
        };
    }

    private String chaine(Map<String, Object> champs, String cle) {
        Object valeur = champs.get(cle);
        if (valeur == null) return null;
        if (valeur instanceof String texte) return texte;
        throw new LigneInvalideException("Type invalide pour " + cle + " : une chaîne est attendue");
    }

    private Boolean booleen(Map<String, Object> champs, String cle) {
        Object valeur = champs.get(cle);
        if (valeur == null) return null;
        if (valeur instanceof Boolean b) return b;
        if (valeur instanceof String s && (s.equalsIgnoreCase("true") || s.equalsIgnoreCase("false"))) {
            return Boolean.valueOf(s);
        }
        throw new LigneInvalideException("Type invalide pour " + cle + " : true ou false est attendu");
    }

    private Double decimal(Map<String, Object> champs, String cle) {
        Object valeur = champs.get(cle);
        if (valeur == null) return null;
        try {
            double resultat = valeur instanceof Number number ? number.doubleValue() :
                    valeur instanceof String s ? Double.parseDouble(s) : Double.NaN;
            if (!Double.isFinite(resultat)) throw new NumberFormatException();
            return resultat;
        } catch (NumberFormatException e) {
            throw new LigneInvalideException("Valeur numérique invalide pour " + cle);
        }
    }

    private Integer entier(Map<String, Object> champs, String cle) {
        Object valeur = champs.get(cle);
        if (valeur == null) return null;
        try {
            if (valeur instanceof String s) return Integer.valueOf(s);
            if (valeur instanceof Number number) return new BigDecimal(number.toString()).intValueExact();
            throw new NumberFormatException();
        } catch (NumberFormatException | ArithmeticException e) {
            throw new LigneInvalideException("Valeur entière invalide pour " + cle);
        }
    }

    private String texte(Object valeur) {
        return valeur == null ? null : valeur.toString();
    }

    private enum Format { JSON, CSV }

    private record LigneImport(int numero, Map<String, Object> champs, String type, String erreur) { }

    private record EnregistrementCsv(int numero, List<String> valeurs) { }

    private static final class ContexteImport {
        private final Set<String> immatriculationsTracteurs = new HashSet<>();
        private final Set<String> immatriculationsBennes = new HashSet<>();
    }

    private static final class LigneInvalideException extends RuntimeException {
        private LigneInvalideException(String message) { super(message); }
    }
}
