# M3 — suivi de progression

## Phase terminée : analyse des PDF et comparaison UML

- **Fonctionnalités terminées :** aucune fonctionnalité applicative; analyse des exigences terminée uniquement.
- **Document créé :** `docs/conception/Analyse-exigences-M3.md`.
- **Fichiers créés :** `M3_PROGRESS.md`, `docs/conception/Analyse-exigences-M3.md`.
- **Fichiers applicatifs modifiés :** aucun.
- **Tests réussis :** aucun test exécuté pendant cette phase documentaire.
- **Erreurs restantes :** intégration M1 sans `TourneeService` livré; aucun socle Frontend M2 dans le dépôt; limites du périmètre d'import inter-modules à confirmer.
- **Prochaines tâches précises :** valider l'analyse; obtenir les contrats M1/M2; arrêter le périmètre API et import; ensuite seulement commencer les entités/repositories M3 et leurs tests.

État Git constaté avant le travail : branche `m3_ressources`; modification locale existante de `src/main/resources/application.yml` (port 8099) préservée. Aucun commit ni push effectué.

## Phase terminée : Backend CRUD des ressources indépendantes

- **Fonctionnalités terminées :** entité abstraite `Ressource`; entités `Ouvrier`, `Tracteur`, `Benne`, `Materiel`; collections MongoDB distinctes; repositories; DTO d'entrée/réponse; services CRUD; endpoints paginés et protégés; lecture par id; validation des champs; réponses 201/200/204/400/401/403/404 selon les cas.
- **Contrat M4 :** `Benne` est persistée dans `bennes` avec les champs `capacite` et `immatriculation` attendus par `MongoBenneReader`.
- **Fichiers créés :** 5 entités, 4 repositories, 8 DTO, 4 services, 4 controllers, 6 fichiers de tests JUnit.
- **Liste des fichiers créés :**
  - Entités : `src/main/java/tn/oliveconnect/m3_ressources/entity/Ressource.java`, `Ouvrier.java`, `Tracteur.java`, `Benne.java`, `Materiel.java`.
  - Repositories : `src/main/java/tn/oliveconnect/m3_ressources/repository/OuvrierRepository.java`, `TracteurRepository.java`, `BenneRepository.java`, `MaterielRepository.java`.
  - DTO : `src/main/java/tn/oliveconnect/m3_ressources/dto/OuvrierRequest.java`, `OuvrierResponse.java`, `TracteurRequest.java`, `TracteurResponse.java`, `BenneRequest.java`, `BenneResponse.java`, `MaterielRequest.java`, `MaterielResponse.java`.
  - Services : `src/main/java/tn/oliveconnect/m3_ressources/service/OuvrierService.java`, `TracteurService.java`, `BenneService.java`, `MaterielService.java`.
  - Controllers : `src/main/java/tn/oliveconnect/m3_ressources/controller/OuvrierController.java`, `TracteurController.java`, `BenneController.java`, `MaterielController.java`.
  - Tests : `src/test/java/tn/oliveconnect/m3_ressources/service/OuvrierServiceTest.java`, `TracteurServiceTest.java`, `BenneServiceTest.java`, `MaterielServiceTest.java`, `src/test/java/tn/oliveconnect/m3_ressources/dto/RessourceRequestValidationTest.java`, `src/test/java/tn/oliveconnect/m3_ressources/controller/RessourceControllerTest.java`.
- **Fichiers modifiés :** `M3_PROGRESS.md` uniquement. `pom.xml`, `application.yml`, `application-dev.yml`, `common`, M1, M2 et M4 n'ont pas été modifiés.
- **Tests réussis :** `mvn test` — BUILD SUCCESS, 84 tests, 0 échec, 0 erreur, 0 ignoré. Le résultat couvre les tests existants et les nouveaux tests CRUD, validation et sécurité HTTP.
- **Compilation :** réussie dans le cycle Maven `test`.
- **Erreurs restantes :** aucune erreur de compilation ou de test connue. Le contrat `TourneeService` M1 et le socle Frontend M2 restent absents du dépôt; les fonctionnalités qui en dépendent sont hors de cette phase. Aucun test d'intégration avec une instance MongoDB réelle n'a été exécuté.
- **Prochaines tâches précises :** poursuivre les fonctions M3 indépendantes non incluses dans cette phase selon la priorité convenue (stock bas/import); avant les affectations, obtenir le contrat de lecture de tournée M1.

Maven a été lancé avec `user.home` redirigé vers `target/`, car l'environnement pointe par défaut sur `C:\.m2` non accessible. Les dépendances téléchargées sont dans `target/.m2`; les fichiers de configuration du projet n'ont pas changé.

## Phase terminée : détection des matériels en stock bas

- **Fonctionnalités terminées :** le service retourne les matériels dont `quantiteStock <= seuilMinimum`, quel que soit leur type (dont `filet` et `vibrateur`), sous forme de `MaterielResponse`. Les éléments dont une des valeurs de comparaison est absente sont ignorés. L'endpoint `GET /api/ressources/materiels/stock-bas` est protégé par `RESPONSABLE_LOGISTIQUE` via la règle de classe déjà présente sur `MaterielController`.
- **Fichiers créés :** aucun.
- **Fichiers modifiés :** `src/main/java/tn/oliveconnect/m3_ressources/service/MaterielService.java`, `src/main/java/tn/oliveconnect/m3_ressources/controller/MaterielController.java`, `src/test/java/tn/oliveconnect/m3_ressources/service/MaterielServiceTest.java`, `src/test/java/tn/oliveconnect/m3_ressources/controller/RessourceControllerTest.java`, `M3_PROGRESS.md`.
- **Tests réussis :** tests service pour stock inférieur, égal, supérieur et liste sans stock bas; tests HTTP pour résultat DTO avec rôle logistique et refus du rôle collecte. Suite complète `mvn test` : BUILD SUCCESS, 90 tests, 0 échec, 0 erreur, 0 ignoré. La compilation a réussi; le CRUD M3 existant est inclus dans la suite et reste passant.
- **Erreurs restantes :** `.\mvnw.cmd test` échoue avant le démarrage de Maven dans l'environnement PowerShell (`Indexation impossible dans un tableau Null`). Le même cycle a été exécuté avec succès via `mvn test` en redirigeant `user.home` vers `target/`. Aucun problème de compilation ou de test applicatif restant connu.
- **Prochaines tâches précises :** aucune dans cette phase; attendre la prochaine priorité M3 validée avant de commencer une autre fonctionnalité.
- **Périmètre préservé :** aucun fichier M1, M2 ou M4, aucune configuration/port, aucune dépendance et aucun fichier CRUD n'ont été modifiés pour cette phase. Aucun commit ni push effectué.

## Phase terminée : recherche des ressources disponibles sur une période

- **Fonctionnalités terminées :** endpoints paginés de recherche pour ouvriers, tracteurs, bennes et matériels. Les paramètres `debut` et `fin` sont obligatoires et attendent une date/heure ISO 8601 (`yyyy-MM-dd'T'HH:mm:ss`); la date de début doit être strictement antérieure à la date de fin. Chaque repository sélectionne les documents `disponibilite=true`; les DTO existants sont retournés dans `PageResponse`.
- **Limite métier :** la période sert à valider la demande, mais la disponibilité retournée reflète uniquement le booléen actuellement enregistré. Aucun conflit de réservation ou d'affectation n'est calculé. Quand M1/Affectation existera, la vérification devra être intégrée dans le Service; aucune dépendance M1 ni classe d'affectation n'a été créée ici.
- **Sécurité :** les routes `/disponibles` autorisent `RESPONSABLE_LOGISTIQUE` et `RESPONSABLE_COLLECTE` au moyen d'une règle méthode. Les autres opérations CRUD gardent leur protection existante.
- **Fichiers créés :** `src/main/java/tn/oliveconnect/m3_ressources/service/PeriodeDisponibilite.java`.
- **Fichiers modifiés :** les quatre controllers `OuvrierController.java`, `TracteurController.java`, `BenneController.java`, `MaterielController.java`; les quatre services `OuvrierService.java`, `TracteurService.java`, `BenneService.java`, `MaterielService.java`; les quatre repositories `OuvrierRepository.java`, `TracteurRepository.java`, `BenneRepository.java`, `MaterielRepository.java`; les tests `OuvrierServiceTest.java`, `TracteurServiceTest.java`, `BenneServiceTest.java`, `MaterielServiceTest.java`, `RessourceControllerTest.java`; ce fichier `M3_PROGRESS.md`.
- **Tests réussis :** couverture des quatre types, DTO/pagination et filtre repository `disponibilite=true`; périodes inversées, paramètres manquants et date non ISO rejetés; accès du rôle collecte et maintien du refus sur le CRUD. Suite Maven complète : BUILD SUCCESS, 97 tests, 0 échec, 0 erreur, 0 ignoré. La compilation a réussi; les tests existants CRUD et stock bas restent passants.
- **Erreurs restantes :** aucune erreur de compilation ou de test connue. Une première exécution a échoué sur une lecture transitoire du JAR Tomcat du cache Maven; la relance complète a réussi sans modification de dépendance.
- **Prochaines tâches précises :** ne pas considérer la disponibilité comme définitive; intégrer ultérieurement la vérification des affectations dans le service quand le contrat M1 sera défini et disponible.
- **Périmètre préservé :** aucun fichier M1, M2 ou M4, aucune classe `Tournee`/`Affectation`, aucune configuration/port et aucune dépendance n'ont été modifiés. Aucun commit ni push effectué.

## Phase terminée : import manuel JSON/CSV des ressources M3

- **Fonctionnalités terminées :** endpoint multipart `POST /api/ressources/import`, réservé à `RESPONSABLE_LOGISTIQUE`. Le service détecte JSON/CSV, lit un tableau JSON ou des lignes CSV, identifie les ressources M3 (ouvrier, tracteur, benne, matériel), convertit strictement chaînes/booléens/nombres, applique les contraintes Jakarta Validation des DTO existants et crée chaque ressource par son service CRUD. Une ligne invalide n'empêche pas l'import des autres; le rapport contient total, importées, rejetées et numéro/motif par erreur. Fichiers vides et formats non pris en charge donnent une erreur HTTP 400.
- **Doublons :** contrôle des entités et repositories M3 : aucun index ni champ métier unique n'est défini. Les DTO d'import ne prennent pas d'ID; MongoDB applique seulement la contrainte automatique sur `_id`. Aucune règle de doublon par nom ou immatriculation n'a été inventée. Les `DuplicateKeyException` retournées par une contrainte réellement présente sont rejetées et détaillées dans le rapport, sans écrasement.
- **Import périodique :** `@EnableScheduling` existe dans `common`, avec tâches existantes de cache et simulation. Aucune source, aucun chemin et aucune fréquence de fichiers M3 n'existent; aucun job d'import n'a été activé. L'import reste manuel jusqu'à définition de la source et de la cadence.
- **Documentation créée :** `docs/conception/Import-ressources-M3.md` avec formats, exemples JSON/CSV, rapport et état du scheduling.
- **Autres fichiers créés :** `src/main/java/tn/oliveconnect/m3_ressources/controller/ImportRessourcesController.java`, `src/main/java/tn/oliveconnect/m3_ressources/service/ImportRessourcesService.java`, `src/main/java/tn/oliveconnect/m3_ressources/dto/ImportErreurResponse.java`, `src/main/java/tn/oliveconnect/m3_ressources/dto/ImportRessourcesResponse.java`, `src/test/java/tn/oliveconnect/m3_ressources/service/ImportRessourcesServiceTest.java`, `src/test/java/tn/oliveconnect/m3_ressources/controller/ImportRessourcesControllerTest.java`.
- **Fichiers modifiés :** `M3_PROGRESS.md` uniquement.
- **Tests réussis :** JSON valide multi-type, CSV valide, lignes invalides et numéros de ligne, contraintes numériques et conversion impossible, fichier vide, format non pris en charge, conflit de contrainte unique Mongo, upload multipart, rôle logistique, refus des autres rôles. Suite complète Maven : BUILD SUCCESS, 113 tests, 0 échec, 0 erreur, 0 ignoré. Compilation réussie; CRUD, stock bas, disponibilité et calendrier précédents restent passants.
- **Erreurs restantes :** aucune erreur applicative connue. La compilation en bac à sable a rencontré une `AccessDeniedException` en fermant un JAR du cache Maven; l'exécution complète Maven autorisée hors bac à sable s'est terminée avec succès.
- **Prochaines tâches précises :** définir une source et une fréquence avant toute activation d'import périodique. Ajouter d'éventuelles règles de doublons seulement si des contraintes uniques métier M3 sont décidées et persistées.
- **Périmètre préservé :** aucun fichier M1, M2 ou M4, aucun fichier CRUD/configuration ni `application.yml` n'a été modifié; port 8099 préservé, aucune dépendance ajoutée. Aucun commit ni push effectué.

## Phase terminée : affectations M3 et conflits de périodes

- **Fonctionnalités terminées :** entité Mongo `Affectation` dans `affectations`; enum `TypeRessource`; repository avec recherche de chevauchement et index composé; DTO de demande/réponse; Service métier; création, consultation paginée par ressource/type et suppression. La création vérifie les dates, l'existence de la ressource dans sa collection exacte, son `disponibilite=true`, puis les affectations déjà enregistrées. Les conflits renvoient HTTP 409; les références inconnues renvoient 404.
- **Règle de chevauchement :** intervalles semi-ouverts `[dateDebut,dateFin)`, avec condition existante `dateDebut < finDemandée && dateFin > débutDemandé`. Les périodes adjacentes sont permises. L'identité de conflit comprend `ressourceId` et `ressourceType`.
- **Statuts actifs :** `ANNULEE` et `TERMINEE` sont traités comme inactifs; tous les autres statuts sont conservés en majuscules et bloquent les périodes chevauchantes. Une nouvelle affectation déjà annulée/terminée ne bloque pas le créneau.
- **Calendrier :** `CalendrierRessourceResponse` inclut maintenant les affectations M3 réelles, filtrées par la période fournie. L'élément calendrier expose id/dates/statut mais omet `tourneeId`.
- **Limite M1 :** `tourneeId` est stocké comme référence externe obligatoire, mais aucune vérification M1 n'est faite. Aucune classe `Tournee`, aucun service M1 ni donnée de tournée n'ont été créés. L'API d'affectation ne prétend pas que la tournée existe.
- **Fichiers créés :** `src/main/java/tn/oliveconnect/m3_ressources/entity/Affectation.java`, `TypeRessource.java`, `src/main/java/tn/oliveconnect/m3_ressources/repository/AffectationRepository.java`, `src/main/java/tn/oliveconnect/m3_ressources/dto/AffectationRequest.java`, `AffectationResponse.java`, `CalendrierAffectationResponse.java`, `src/main/java/tn/oliveconnect/m3_ressources/service/AffectationService.java`, `src/main/java/tn/oliveconnect/m3_ressources/controller/AffectationController.java`, `src/test/java/tn/oliveconnect/m3_ressources/service/AffectationServiceTest.java`, `src/test/java/tn/oliveconnect/m3_ressources/controller/AffectationControllerTest.java`, `docs/conception/Affectations-M3.md`.
- **Fichiers modifiés :** `src/main/java/tn/oliveconnect/m3_ressources/service/CalendrierService.java`, `src/main/java/tn/oliveconnect/m3_ressources/dto/CalendrierRessourceResponse.java`, `src/test/java/tn/oliveconnect/m3_ressources/service/CalendrierServiceTest.java`, `src/test/java/tn/oliveconnect/m3_ressources/controller/RessourceControllerTest.java`, `M3_PROGRESS.md`.
- **Tests réussis :** création valide, ressource absente/indisponible, dates invalides, chevauchement, adjacent, ressource distincte même période, même ID entre types, statut annulé, suppression/404, consultation paginée, sécurité des rôles, enrichissement du calendrier sans `tourneeId`. Suite complète Maven : BUILD SUCCESS, 126 tests, 0 échec, 0 erreur, 0 ignoré. Les tests des phases précédentes (CRUD, stock bas, disponibilité, calendrier et import) restent passants.
- **Erreurs restantes :** aucune erreur connue. La compilation sandboxée rencontre l'AccessDeniedException déjà observée lors de la fermeture du JAR du cache Maven; la suite complète autorisée hors sandbox a réussi.
- **Prochaines tâches précises :** attendre le contrat M1 avant d'ajouter toute vérification ou affichage des tournées; ne pas interpréter `tourneeId` comme confirmé.
- **Périmètre préservé :** aucun fichier M1, M2 ou M4, aucun `application.yml` ni port n'a été modifié; aucune dépendance ajoutée. Aucun commit ni push effectué.

## Phase terminée : calendrier interne des ressources

- **Fonctionnalités terminées :** `GET /api/calendrier?ressourceId={id}` consulte un ouvrier, tracteur, benne ou matériel. Les paramètres facultatifs `debut` et `fin` acceptent ISO 8601; ils doivent être fournis ensemble et `debut` doit précéder `fin`. Sans dates, la réponse contient seulement les informations générales. Avec dates, la réponse inclut la période demandée. Les réponses utilisent des DTO calendrier dédiés; aucune entité MongoDB n'est exposée.
- **Règles d'identification :** recherche par ID dans les quatre collections; un ID absent donne 404. Si le même ID apparaît dans plusieurs collections, le service rejette la demande comme ambiguë (400).
- **Limite métier :** ce calendrier interne rapporte uniquement le champ `disponibilite` actuellement enregistré et fait écho à la période demandée. M3 ne possède pas encore de périodes de réservation; aucune disponibilité future, affectation ou tournée n'est calculée ou simulée. Les affectations M1 et les tournées seront intégrées ultérieurement dans le Service quand leurs contrats seront disponibles.
- **Sécurité :** endpoint protégé par `isAuthenticated()`; la politique commune JWT exige une authentification. Les protections des autres routes n'ont pas été modifiées.
- **Fichiers créés :** `src/main/java/tn/oliveconnect/m3_ressources/dto/CalendrierRessourceResponse.java`, `src/main/java/tn/oliveconnect/m3_ressources/dto/PeriodeCalendrierResponse.java`, `src/main/java/tn/oliveconnect/m3_ressources/service/CalendrierService.java`, `src/main/java/tn/oliveconnect/m3_ressources/controller/CalendrierController.java`, `src/test/java/tn/oliveconnect/m3_ressources/service/CalendrierServiceTest.java`.
- **Fichiers modifiés :** `src/test/java/tn/oliveconnect/m3_ressources/controller/RessourceControllerTest.java`, `M3_PROGRESS.md`.
- **Tests réussis :** couverture des quatre types, disponibilité actuelle, période fournie/absente, période inversée/partielle, 404 ressource absente, dates non ISO et paramètres manquants, authentification obligatoire. Suite Maven complète : BUILD SUCCESS, 104 tests, 0 échec, 0 erreur, 0 ignoré. Compilation réussie; tests CRUD, stock bas et disponibilité précédente inclus et passants.
- **Erreurs restantes :** aucune erreur de compilation ou de test connue. Des exécutions initiales ont rencontré une erreur d'accès transitoire au JAR du cache Maven et ont révélé une erreur d'import/test corrigée avant la réussite complète.
- **Prochaines tâches précises :** attendre la disponibilité des contrats M1 pour intégrer, dans le Service calendrier, les affectations et les tournées réelles; ne pas considérer les périodes demandées comme des réservations.
- **Périmètre préservé :** aucun fichier M1, M2 ou M4, aucune classe `Tournee`/`Affectation`, aucune configuration/port et aucune dépendance n'ont été modifiés. Aucun commit ni push effectué.

## Correctif : disponibilité tenant compte des affectations M3

- **Fonctionnalité :** les quatre recherches `disponibles` conservent le filtre `disponibilite=true`, retirent les ressources ayant une affectation active de même `ressourceType` dont les dates chevauchent la période demandée, puis appliquent la pagination sur les résultats filtrés.
- **Intervalles :** les critères Mongo dérivés `dateDebut < finDemandée` et `dateFin > debutDemandé` implémentent `[début, fin)`; une affectation qui se termine au début ou commence à la fin est exclue des conflits.
- **Statuts retenus :** conformément à l'enum `StatutTournee` du diagramme UML et à la règle existante dans `AffectationService`, `PLANIFIEE` et `EN_COURS` sont actifs; `ANNULEE` et `TERMINEE` n'occupent pas la période. Aucun nouveau statut n'a été introduit.
- **Fichiers modifiés :** `service/OuvrierService.java`, `TracteurService.java`, `BenneService.java`, `MaterielService.java`, `AffectationService.java`; `repository/OuvrierRepository.java`, `TracteurRepository.java`, `BenneRepository.java`, `MaterielRepository.java`, `AffectationRepository.java`; tests services des quatre types et `AffectationServiceTest.java`.
- **Fichiers créés :** `service/RechercheDisponibiliteService.java` et son test `RechercheDisponibiliteServiceTest.java`.
- **Tests :** absence d'affectation, chevauchement total et partiel, bornes adjacentes, type de ressource exact, ressource indisponible; suite Maven complète : BUILD SUCCESS, 134 tests, 0 échec, 0 erreur, 0 ignoré.
- **Contraintes :** aucun endpoint, DTO, fichier M1/M2/M4, dépendance, port ou configuration n'a été modifié. Aucun commit ni push effectué.

## Phase terminée : détection des doublons tracteurs/bennes à l'import

- **Règles appliquées :** aucun dédoublonnage pour ouvriers ou matériels. Les immatriculations sont uniques à l'import à l'intérieur de chaque collection (`tracteurs` ou `bennes`), sans comparaison croisée entre les deux collections.
- **Normalisation :** une classe réutilisable enlève les espaces extérieurs pour la valeur importée et construit une clé insensible à la casse. Les espaces internes sont conservés. La recherche Mongo tolère les espaces extérieurs en base et utilise l'option regex `i`.
- **Import de fichier :** les doublons de lignes sont contrôlés par collection; seule la première ligne valide est importée. Chaque répétition et chaque immatriculation déjà présente est rejetée avec le numéro de ligne et un motif explicite; le traitement continue sur les lignes suivantes.
- **MongoDB :** contrôles d'existence dans les repositories existants, sans index unique ni migration/suppression de données.
- **Fichiers créés :** `src/main/java/tn/oliveconnect/m3_ressources/service/ImmatriculationNormalizer.java`, `src/test/java/tn/oliveconnect/m3_ressources/service/ImmatriculationNormalizerTest.java`.
- **Fichiers modifiés :** repositories et services Tracteur/Benne, `ImportRessourcesService.java`, tests d'import et tests Tracteur/Benne, `docs/conception/Import-ressources-M3.md`.
- **Tests :** JSON et CSV, doublons en fichier, équivalence casse/espaces, valeur existante par collection, indépendance Tracteur/Benne, import répété d'ouvriers/matériels, rapport et poursuite des lignes. Suite complète Maven : BUILD SUCCESS, 144 tests, 0 échec, 0 erreur, 0 ignoré.
- **Limite documentée :** sans index unique (exclu de cette phase), deux requêtes concurrentes peuvent théoriquement passer le contrôle avant insertion.
- **Contraintes :** aucun fichier M1/M2/M4, endpoint, DTO, configuration/port, dépendance ou donnée existante modifiés; aucun commit ni push.

## Phase terminée : sérialisation locale des créations d'affectations

- **Protection :** ajout de `VerrouAffectationRessource`, verrou en mémoire indexé par `(TypeRessource, ressourceId)`. `AffectationService` garde le verrou autour de la recherche des chevauchements actifs et de la sauvegarde. Les contrôles préalables existants restent inchangés.
- **Libération et mémoire :** le verrou est libéré dans un `finally`. Un comptage atomique par clé dans `ConcurrentHashMap.compute` conserve les verrous pendant que des threads attendent et retire chaque entrée quand il n'y a plus d'utilisateur; les clés sont indépendantes et ne créent pas d'interblocage entre ressources.
- **Règles préservées :** statuts `ANNULEE`/`TERMINEE` inactifs; intervalles semi-ouverts et conflit HTTP 409 inchangés; la clé inclut le type, donc un même ID dans deux types ne partage pas le verrou.
- **Fichiers créés :** `src/main/java/tn/oliveconnect/m3_ressources/service/VerrouAffectationRessource.java`, `src/test/java/tn/oliveconnect/m3_ressources/service/AffectationServiceConcurrencyTest.java`.
- **Fichiers modifiés :** `src/main/java/tn/oliveconnect/m3_ressources/service/AffectationService.java`, `src/test/java/tn/oliveconnect/m3_ressources/service/AffectationServiceTest.java`, `src/test/java/tn/oliveconnect/m3_ressources/controller/AffectationControllerTest.java`.
- **Tests :** requêtes HTTP réellement concurrentes sur la même ressource (un 201, un 409, une insertion); deux ressources distinctes progressent en parallèle; même ID sur deux types progresse en parallèle; reprise depuis un autre thread après exception de sauvegarde; affectations existantes `ANNULEE` et `TERMINEE` n'empêchent pas une nouvelle active. Suite complète : BUILD SUCCESS, 152 tests, 0 échec, 0 erreur, 0 ignoré.
- **Limite :** le verrou protège les threads de cette seule instance JVM. Il ne coordonne ni plusieurs instances de l'application, ni les écritures directes dans MongoDB.
- **Contraintes :** aucune dépendance, transaction ou modification de M1/M2/M4/configuration/endpoint/DTO/donnée n'a été ajoutée. Aucun commit ni push effectué.

## Vérification documentaire de l'index Affectation

- **Vérification :** `Affectation.java` conserve l'index existant `idx_affectations_ressource_periode` avec `{ressourceId: 1, ressourceType: 1, dateDebut: 1, dateFin: 1}`.
- **Documentation :** `docs/conception/Affectations-M3.md` précise désormais l'utilité de son préfixe pour les accès par ressource et la détection des chevauchements. Elle note qu'un index commençant par `ressourceType` pourrait être étudié pour la recherche de disponibilité par type, uniquement si les mesures le justifient.
- **Périmètre :** aucun index ajouté ou supprimé; aucune requête, repository, configuration, fichier M1/M2/M4, dépendance ou donnée modifiée. Aucun commit ni push effectué.

## Mise à jour documentaire du calendrier M3

- **Fichier modifié :** `src/main/java/tn/oliveconnect/m3_ressources/service/CalendrierService.java`; ses commentaires précisent maintenant que les affectations réelles de la collection M3 sont retournées (toutes sans période, filtrées par chevauchement si une période est fournie), tandis que les détails de tournée M1 ne sont pas consultés et que `tourneeId` reste opaque/non exposé.
- **Vérification :** `CalendrierServiceTest` : 5 tests réussis, 0 échec, 0 erreur; le test existant confirme le retour des affectations M3 réelles et le filtrage de période.
- **Périmètre :** commentaires uniquement dans le service; aucune logique métier, endpoint, DTO, configuration ou autre module modifié.
