# Import manuel des ressources M3

L'endpoint d'import accepte un multipart `file` en JSON ou CSV. Il ne traite que les ouvriers, tracteurs, bennes et matériels. Les lignes valides sont créées via les services M3 existants; les erreurs de validation sont rapportées par numéro de ligne sans annuler les autres lignes valides.

## JSON

Le fichier JSON est un tableau d'objets. Le champ `ressourceType` (valeurs `ouvrier`, `tracteur`, `benne`, `materiel`) précise le type, notamment pour un fichier mixte. Il peut être omis si les champs métier permettent un type unique.

```json
[
  {"ressourceType":"ouvrier","nom":"Amina","disponibilite":true,"specialite":"Taille","saisonnier":false},
  {"ressourceType":"tracteur","nom":"Tracteur A","disponibilite":true,"immatriculation":"123TU456","puissance":90.5},
  {"ressourceType":"benne","nom":"Benne A","disponibilite":true,"immatriculation":"789TU456","capacite":1500},
  {"ressourceType":"materiel","nom":"Filets","disponibilite":true,"type":"filet","quantiteStock":12,"seuilMinimum":3}
]
```

Les booléens JSON doivent être des booléens et non des chaînes. Les nombres doivent respecter le type du modèle; les puissances/capacités doivent être positives et les stocks/seuils positifs ou nuls.

## CSV

Le CSV est UTF-8, utilise la virgule comme séparateur et sa première ligne comme en-tête. Pour mélanger les types dans un même CSV, fournir `ressourceType` et les colonnes utiles à chaque type. Les champs texte contenant virgule ou guillemets peuvent être cités selon les règles CSV.

```csv
ressourceType,nom,disponibilite,specialite,saisonnier,immatriculation,puissance,capacite,type,quantiteStock,seuilMinimum
ouvrier,Amina,true,Taille,false,,,,,,
tracteur,Tracteur A,true,,,123TU456,90.5,,,,
benne,Benne A,true,,,789TU456,,1500,,
materiel,Filets,true,,,,,,filet,12,3
```

## Rapport

```json
{
  "totalLignes": 10,
  "nombreImportes": 8,
  "nombreRejetes": 2,
  "erreurs": [
    {"ligne": 3, "motif": "capacite doit être strictement positive"},
    {"ligne": 7, "motif": "nom est obligatoire"}
  ]
}
```

Pour JSON, `ligne` identifie l'index de l'objet (à partir de 1). Pour CSV, il indique le numéro de ligne physique (l'en-tête est ligne 1).

## Doublons et import périodique

Les règles d'import validées sont les suivantes :

- **Tracteurs :** l'immatriculation est comparée uniquement dans la collection `tracteurs`.
- **Bennes :** l'immatriculation est comparée uniquement dans la collection `bennes`; une immatriculation identique dans `tracteurs` n'est pas un doublon de benne.
- Pour les tracteurs et les bennes, la comparaison retire les espaces extérieurs et ignore la casse. Les espaces internes restent significatifs. La valeur importée est stockée sans ses espaces extérieurs et conserve la casse de la première ligne acceptée.
- Une immatriculation déjà présente dans la collection correspondante est rejetée avant insertion. Dans le même fichier, la première ligne valide est importée; les lignes suivantes équivalentes sont rejetées avec leur numéro et un motif dans le rapport. Un rejet sur une ligne ne bloque pas les suivantes.
- **Ouvriers et matériels :** aucun doublon métier n'est détecté automatiquement. Le nom, la spécialité, le statut saisonnier, le type et les quantités ne sont pas utilisés comme identifiants.
- Aucun index unique n'est créé et aucune donnée existante n'est modifiée ou supprimée. Le contrôle applicatif d'existence n'est pas atomique entre plusieurs imports concurrents; l'absence d'index unique, demandée pour cette phase, laisse cette limite.

Le scheduling Spring est activé dans `common`, pour des tâches existantes de cache et de simulation. Aucune source de fichiers, aucun chemin M3 ni aucune fréquence d'import M3 ne sont définis. L'import reste manuel via l'endpoint; aucun job périodique n'est activé.
