# Analyse des exigences M3 â€” OliveConnect

**Statut :** analyse documentaire terminée, développement non commencé.
**Sources :** `docs/cadrage/OliveConnect-Ã‰tape-1-Cadrage.pdf` (14 pages), `docs/plan/Plan_OliveConnect.pdf` (6 pages), `docs/conception/M3.png` et `docs/conception/OliveConnect_diagramme_classes.puml`.

## 1. RÃ©sumÃ© des exigences obligatoires

M3 est le module de **gestion des ressources**. Son acteur principal est le **Responsable logistique**; le **Responsable de collecte** consulte les disponibilitÃ©s et peut participer aux affectations. Le but mÃ©tier est de rendre disponibles les ouvriers, tracteurs et bennes nÃ©cessaires avant la planification d'une tournÃ©e.

Les deux PDF exigent les tÃ¢ches suivantes :

1. GÃ©rer les ouvriers, tracteurs, bennes et matÃ©riel (ajout, modification et suppression).
2. Consulter les ressources libres pour une pÃ©riode et calculer leur disponibilitÃ© Ã  partir des affectations.
3. Affecter des ressources Ã  une tournÃ©e M1 et dÃ©tecter les conflits de disponibilitÃ©.
4. PrÃ©senter un calendrier des tournÃ©es par ressource.
5. Signaler le stock bas, explicitement pour les **filets** et les **vibrateurs**, selon les seuils de stock.
6. Importer pÃ©riodiquement des donnÃ©es externes simulÃ©es JSON/CSV aprÃ¨s validation de conformitÃ© et journaliser les rejets.
7. Fournir les Ã©crans de gestion des ressources, le calendrier par ressource et l'Ã©cran d'affectation.
8. Tester les scÃ©narios nominaux et alternatifs avec JUnit et fournir une collection Postman par module.

Le plan classe M3 comme une charge moyenne, dÃ©limitÃ©e autour de cinq entitÃ©s, des conflits, du calendrier et de l'import pÃ©riodique. Il prÃ©voit un backend sans Frontend, une dÃ©monstration et correction des Ã©carts de contrat, puis le Frontend, puis l'intÃ©gration. (Plan, p. 2â€“6; Cadrage, p. 2, 6, 9, 13.)

## 2. Classes et attributs exacts

Les classes et attributs ci-dessous sont ceux du diagramme de classes UML. Les types sont conservÃ©s tels qu'ils y sont Ã©crits.

| Classe | Attributs exacts |
|---|---|
| `Ressource` (abstraite) | `id : String`, `nom : String`, `disponibilite : Boolean` |
| `Ouvrier` | `specialite : String`, `saisonnier : Boolean` |
| `Tracteur` | `immatriculation : String`, `puissance : Double` |
| `Benne` | `capacite : Double`, `immatriculation : String` |
| `Materiel` | `type : String`, `quantiteStock : Integer`, `seuilMinimum : Integer` |
| `Affectation` | `id : String`, `dateDebut : LocalDateTime`, `dateFin : LocalDateTime`, `statut : String` |

Les sous-classes de `Ressource` hÃ©ritent de `id`, `nom` et `disponibilite`. Les associations UML sont : une tournÃ©e M1 a `0..*` affectations; une ressource a `0..*` affectations; chaque affectation dÃ©signe une tournÃ©e et une ressource. Le diagramme ne dÃ©finit pas de classe `Tournee` M3.

### Collections MongoDB et champs

Le cadrage impose une collection par type concret, plutÃ´t qu'une collection unique d'hÃ©ritage :

- `ouvriers` : `_id`, `nom` et attributs propres (`specialite`, `saisonnier`), `disponibilite`.
- `tracteurs` : `_id`, `nom` et attributs propres (`immatriculation`, `puissance`), `disponibilite`.
- `bennes` : `_id`, `nom` et attributs propres (`capacite`, `immatriculation`), `disponibilite`.
- `materiels` : `_id`, `nom` et attributs propres (`type`, `quantiteStock`, `seuilMinimum`), `disponibilite`.
- `affectations` : `_id`, `ressourceId`, `ressourceType`, `tourneeId`, `dateDebut`, `dateFin`, `statut`.

`ressourceType` est stockÃ© comme chaÃ®ne d'Ã©numÃ©ration : `OUVRIER`, `TRACTEUR`, `BENNE` ou `MATERIEL`. Une affectation est la collection d'association entre tournÃ©e et ressource. (Cadrage, p. 4â€“5.)

Les index explicitement indiquÃ©s sont `affectations(ressourceId, dateDebut, dateFin)` pour les conflits et `tournees(statut, dateDebut)` pour le calendrier. Les recherches de disponibilitÃ© devront donc utiliser les identifiants, les dates et le type de ressource sans fusionner les collections des autres modules. (Cadrage, p. 5.)

## 3. Backend requis

### Services et rÃ¨gles mÃ©tier

- CRUD des quatre types de ressources.
- Recherche des ressources disponibles sur une pÃ©riode.
- Affectation Ã  une tournÃ©e M1, lecture et retrait d'affectations.
- DÃ©tection des pÃ©riodes qui se chevauchent et calcul de disponibilitÃ©.
- Seuils de stock et dÃ©tection du stock bas, notamment filets et vibrateurs.
- Calendrier organisÃ© par ressource et par tournÃ©e.
- Import planifiÃ© JSON/CSV, validation de conformitÃ©, dÃ©tection/rejet des donnÃ©es invalides et journalisation des rejets.

Le cadrage prÃ©cise que les rÃ¨gles mÃ©tier sont dans les Services, jamais dans les Controllers; les entitÃ©s MongoDB ne sont pas retournÃ©es directement. Les erreurs doivent suivre le format commun. Un conflit d'affectation correspond au code HTTP `409`. (Cadrage, p. 6, 9, 13.)

Le cadrage ne fixe pas la rÃ¨gle des intervalles qui se touchent exactement. La mission fournie dans la conversation demande de les accepter s'ils ne se chevauchent pas; l'implÃ©mentation proposÃ©e utilisera donc des intervalles semi-ouverts `[dÃ©but, fin)` et vÃ©rifiera aussi les modifications d'affectation.

### API exacte du cadrage

Toutes les routes ci-dessous sont prÃ©fixÃ©es par `/api` et protÃ©gÃ©es par rÃ´le selon le tableau du cadrage.

| MÃ©thode | Chemin | RÃ´le autorisÃ© | Fonction |
|---|---|---|---|
| `GET`, `POST` | `/api/ressources/{type}` | Responsable logistique | Lister, ajouter; `{type}` vaut `ouvriers`, `tracteurs`, `bennes` ou `materiels` |
| `PUT`, `DELETE` | `/api/ressources/{type}/{id}` | Responsable logistique | Modifier, supprimer |
| `GET` | `/api/ressources/{type}/disponibles?debut=&fin=` | Responsable logistique, Responsable de collecte | Lister les ressources libres sur la pÃ©riode |
| `GET` | `/api/ressources/materiels/stock-bas` | Responsable logistique | Lister les stocks sous seuil |
| `POST` | `/api/affectations` | Responsable logistique, Responsable de collecte | Affecter; `409` si conflit |
| `DELETE` | `/api/affectations/{id}` | Responsable logistique | Retirer une affectation |
| `GET` | `/api/calendrier?ressourceId=` | Utilisateur connectÃ© | Calendrier d'une ressource |

Les conventions communes sont : JSON, dates ISO 8601, identifiants en chaÃ®ne, pagination `{content, page, size, totalElements}`, `@PreAuthorize` sur chaque endpoint, erreurs au format commun (Cadrage, p. 6, 9, 13).

### Import

Les PDF nomment une source simulÃ©e de fichiers JSON/CSV contenant les rÃ©fÃ©rentiels **vergers, variÃ©tÃ©s, ressources et pressoirs** (Cadrage, p. 1). Le plan confie Ã  M3 l'import des donnÃ©es externes simulÃ©es, une tÃ¢che `@Scheduled`, la validation et la journalisation des rejets (Plan, p. 2â€“4). Les PDF ne spÃ©cifient ni cadence, ni schÃ©ma de fichier, ni endpoint d'upload, ni politique dÃ©taillÃ©e de doublons.

L'import des ressources est dans le pÃ©rimÃ¨tre M3. L'import des vergers/variÃ©tÃ©s et pressoirs touche les collections possÃ©dÃ©es par M1/M4 : il faut donc passer par leurs contrats de service ou obtenir une coordination explicite avant tout Ã©criture dans ces collections.

## 4. Frontend requis

Les exigences Frontend propres Ã  M3 dans le plan sont :

- Ã©crans de gestion des ressources;
- calendrier des tournÃ©es par ressource;
- Ã©cran d'affectation des ressources aux tournÃ©es.

Le rÃ´le Responsable de collecte doit pouvoir consulter les disponibilitÃ©s avant de planifier. Les PDF ne dÃ©finissent pas d'exigence pour un tableau de bord M3, une navigation latÃ©rale, les formulaires dÃ©taillÃ©s, les filtres ou la direction artistique. Le tableau de bord global est attribuÃ© Ã  M4 et calculÃ© par agrÃ©gation. (Plan, p. 3â€“4; Cadrage, p. 2, 9â€“10.)

Le plan attribue Ã  M2 la structure commune du Frontend (layout, responsive, aide en ligne, visite). M3 doit donc fournir ses Ã©crans intÃ©grables dans ce socle. Le dÃ©pÃ´t examinÃ© ne contient actuellement ni application Frontend ni socle M2; ce contrat d'intÃ©gration reste Ã  confirmer avec l'Ã©quipe. (Plan, p. 2â€“3.)

## 5. Comparaison avec les diagrammes

| Ã‰lÃ©ment | `M3.png` et PlantUML | PDF | Conclusion |
|---|---|---|---|
| ModÃ¨le de ressources | `Ressource` abstraite et quatre sous-classes avec les attributs du tableau ci-dessus | Collections Mongo sÃ©parÃ©es par type concret | Conforme; le modÃ¨le Java peut partager une superclasse, mais la persistance reste sÃ©parÃ©e |
| Affectation | Dates et statut; relation vers une ressource et une tournÃ©e | Champs rÃ©fÃ©rencÃ©s `ressourceId`, `ressourceType`, `tourneeId`; collection sÃ©parÃ©e | Conforme; le type est nÃ©cessaire pour retrouver la collection de ressource |
| DisponibilitÃ© | Boolean hÃ©ritÃ© de `Ressource` | Boolean stockÃ© sur chaque ressource; recherche par pÃ©riode via les affectations | ComplÃ©mentaire : disponibilitÃ© ponctuelle et disponibilitÃ© calculÃ©e sur une pÃ©riode doivent Ãªtre distinguÃ©es |
| MatÃ©riel | `type`, `quantiteStock`, `seuilMinimum` | Alerte de stock bas, explicitement filets/vibrateurs | Conforme; le seuil est le support de la rÃ¨gle |
| TournÃ©e | RÃ©fÃ©rence Ã  la classe M1; aucune entitÃ© tournÃ©e M3 | M3 doit lire `TourneeService` de M1 | Conforme; ne pas crÃ©er une deuxiÃ¨me classe `Tournee` |
| Benne | `capacite`, `immatriculation`; relation d'usage par M4 `Reception` | M4 lit les bennes par rÃ©fÃ©rence | Conforme si la collection `bennes` conserve ces deux champs et des identifiants compatibles |
| Import | Pas de classe d'import dans le diagramme | Import pÃ©riodique JSON/CSV, validation, rejet journalisÃ© | FonctionnalitÃ© de service hors diagramme; aucune entitÃ© supplÃ©mentaire n'est exigÃ©e par les PDF |
| Endpoints/Ã©crans | Non dÃ©crits par UML | Routes et trois Ã©crans M3 listÃ©s sÃ©parÃ©ment | Aucun Ã©cart de modÃ¨le; l'UML ne dÃ©crit pas l'ensemble des interfaces |

Les deux diagrammes M3 concordent sur les cinq classes mÃ©tier et leurs attributs. Le cadrage prÃ©cise la matÃ©rialisation Mongo et les rÃ©fÃ©rences, sans demander de modifier les diagrammes. Il ajoute Ã©galement des corrections sur des relations globales M1/M4, sans changer les classes M3. (Cadrage, p. 3â€“5.)

## 6. DÃ©pendances et problÃ¨mes d'intÃ©gration

### M1 â€” tournÃ©es

Le cadrage dit explicitement : **M3 appelle `TourneeService` M1 pour lire la tournÃ©e lors d'une affectation** (Cadrage, p. 10). La tournÃ©e possÃ¨de notamment `dateDebut`, `dateFin`, `statut`; elle reste la propriÃ©tÃ© de M1. Dans le dÃ©pÃ´t actuel, `m1_collectes` ne contient que des `.gitkeep` et aucun `TourneeService` n'est livrÃ©. Il faut convenir de ce contrat avant d'implÃ©menter l'affectation. Une lecture directe de la collection `tournees` par M3 serait un contournement de la rÃ¨gle de cadrage Â« via Service, jamais via les entitÃ©s Â».

Le cadrage mentionne aussi un service de notification M2 utilisÃ© par M1 pendant la planification, mais ne demande pas explicitement Ã  M3 d'appeler M2 pour les alertes de stock. L'alerte de stock bas M3 doit donc fonctionner dans son pÃ©rimÃ¨tre sans inventer de contrat M2; une notification partagÃ©e pourra Ãªtre ajoutÃ©e aprÃ¨s accord sur le service.

### M2 â€” socle Frontend et notifications

Le plan attribue Ã  M2 la structure commune du Frontend, mais aucun fichier Frontend M2 n'est prÃ©sent. Les Ã©crans M3 devront s'intÃ©grer au layout livrÃ© par M2 ou, en son absence, faire l'objet d'un choix d'architecture avant de crÃ©er une application autonome.

### M4 â€” bennes

Le contrat existant dans le code est en lecture seule : `BenneReader.findById(String)` retourne `BenneRef(id, immatriculation, capacite)`. `MongoBenneReader` lit la collection Mongo `bennes` et projette les champs `immatriculation` et `capacite`. M3 doit conserver exactement ces noms et fournir un `_id` acceptÃ© par M4. Aucun changement au code M4 n'est nÃ©cessaire selon le contrat actuel.

### Ã‰carts entre les PDF et la mission dÃ©taillÃ©e de cette conversation

Ces demandes supplÃ©mentaires peuvent Ãªtre traitÃ©es comme extensions, mais elles ne sont pas listÃ©es comme exigences exactes des PDF :

- lecture d'une ressource par identifiant (`GET .../{id}`), liste/dÃ©tail des affectations, modification d'une affectation;
- filtres calendrier jour/semaine, disponibilitÃ© libre/occupÃ©e;
- endpoint d'upload JSON/CSV et rapport d'import dÃ©taillÃ©;
- tableau de bord dÃ©diÃ© M3 et ses chiffres, alors que le tableau de bord global appartient Ã  M4 dans les PDF;
- dÃ©sactivation logique, recherche avancÃ©e et gestion de disponibilitÃ© manuelle;
- notification M2 lors d'un stock bas.

Ces extensions demandent des routes ou choix de donnÃ©es non fixÃ©s par le contrat PDF. Elles doivent Ãªtre identifiÃ©es comme extensions M3 et vÃ©rifiÃ©es contre les contrats communs avant d'Ãªtre exposÃ©es.

## 7. Fichiers prÃ©vus

La liste dÃ©finit une cible de conception, pas des fichiers dÃ©jÃ  crÃ©Ã©s. Les noms prÃ©cis pourront Ãªtre regroupÃ©s si l'architecture retenue le permet.

### Backend M3

- `m3_ressources/entity/` : `Ressource`, `Ouvrier`, `Tracteur`, `Benne`, `Materiel`, `Affectation`.
- `m3_ressources/dto/` : requÃªtes/rÃ©ponses CRUD de ressources, disponibilitÃ©, affectation, calendrier et rÃ©sultat d'import.
- `m3_ressources/repository/` : dÃ©pÃ´ts des quatre collections et des affectations; requÃªtes de chevauchement et disponibilitÃ©.
- `m3_ressources/service/` : services des ressources, affectations, calendrier, stock et import planifiÃ©.
- `m3_ressources/controller/` : `RessourceController`, `AffectationController`, `CalendrierController`; `ImportController` seulement si l'upload demandÃ© dans la mission est retenu.
- `m3_ressources/integration/` : port/adaptateur de lecture de tournÃ©e, aprÃ¨s accord et livraison du contrat M1.
- `src/test/java/tn/oliveconnect/m3_ressources/` : tests unitaires des services et rÃ¨gles; tests d'API/intÃ©gration selon les conventions existantes.
- `docs/OliveConnect-M3.postman_collection.json` : collection d'appels M3.
- `docs/M3-API.md` : routes, rÃ´les, exemples et codes d'erreur.

### Frontend M3

Aucune technologie Frontend n'est actuellement prÃ©sente dans le dÃ©pÃ´t. Sous rÃ©serve du socle M2 :

- `frontend/src/features/m3/api/` : client et types des API M3;
- `frontend/src/features/m3/pages/` : gestion des ressources, affectation, calendrier, stocks/import si extensions retenues;
- `frontend/src/features/m3/components/` : tableaux, formulaires, disponibilitÃ©, conflits et alertes;
- tests de composants selon l'outillage Frontend retenu.

Le chemin `frontend/` est une proposition Ã  valider avec M2, pas une structure existante.

## 8. Tests exigÃ©s et Ã©tat actuel

Les PDF exigent des tests JUnit avec scÃ©narios nominaux et alternatifs et une collection Postman par module (Cadrage, p. 13). Le plan prÃ©cise que M3 participe aux tests JUnit des rÃ¨gles mÃ©tier avec M1 (Plan, p. 6). Les PDF ne donnent pas de liste exhaustive de cas de test.

La mission fournie dans la conversation ajoute des cas Ã  couvrir : CRUD des ressources, quantitÃ©s et seuils, stock bas, affectation, conflits/chevauchements, annulation, disponibilitÃ©, import JSON/CSV, erreurs et compatibilitÃ© M1/M4. Ces cas complÃ¨tent les exigences des PDF.

**Aucun test ni build n'a Ã©tÃ© exÃ©cutÃ© pendant cette analyse. Aucun fichier de code applicatif n'a Ã©tÃ© crÃ©Ã© ou modifiÃ©.**

## 9. DÃ©cisions d'intÃ©gration Ã  prendre avant développement

1. Obtenir le contrat de `TourneeService` M1 (lecture par id, dates, statut) et les possibilitÃ©s de lister/choisir les tournÃ©es pour le Frontend.
2. Confirmer le socle Frontend M2 et la technologie/structure rÃ©ellement retenue.
3. Confirmer le pÃ©rimÃ¨tre de l'import : ressources M3 uniquement, ou rÃ©fÃ©rentiels M1/M4 par appel Ã  leurs services.
4. SÃ©parer le contrat PDF des extensions demandÃ©es dans la mission (dashboard M3, endpoints supplÃ©mentaires, upload, historique et filtres calendrier).
5. Valider le comportement d'une suppression d'une ressource dÃ©jÃ  affectÃ©e et la gestion des affectations en cours/annulÃ©es.
