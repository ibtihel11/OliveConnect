# OliveConnect

Plateforme web de gestion de la collecte d'olives pour une coopérative de la région de Sfax.
Projet de groupe (4 membres) – 2ème année ING INFO : Bases de données massives, J2EE, Administration des serveurs Web.

**Stack :** Spring Boot (API REST) · Spring Data MongoDB · Spring Security (JWT) · Maven

## Prérequis

- **JDK 21**
- **MongoDB** lancé en local sur `localhost:27017` (base `oliveconnect`)
- Maven : inutile de l'installer, le wrapper `mvnw` est fourni
- Postman (pour tester les API)

## Lancer le projet

```bash
./mvnw spring-boot:run        # Windows : mvnw.cmd spring-boot:run
./mvnw test                   # lancer les tests
```

API disponible sur `http://localhost:8080/api`.

## Qui travaille où

Chacun travaille **uniquement dans son package** (`src/main/java/tn/oliveconnect/`), avec les couches `controller / service / repository / entity / dto`.

| Membre | Fonctionnalité | Dossier |
|---|---|---|
| **M1** | Collectes et vergers (+ authentification et droits) | `m1_collectes/` |
| **M2** | Alertes terrain et notifications (+ structure du front) | `m2_alertes/` |
| **M3** | Ressources (+ import des données simulées) | `m3_ressources/` |
| **M4** | Carte, tableau de bord, pressoir (+ serveur, supervision, sauvegarde) | `m4_carte/` |
| Tous | Configuration, sécurité, gestion des erreurs, DTO communs | `common/` (à ne modifier qu'avec l'accord de l'équipe) |

## Documentation

Tout est dans le dossier [`docs/`](docs/) :

| Dossier | Contenu |
|---|---|
| [`docs/cadrage/`](docs/cadrage/) | Étape 1 : acteurs, schéma NoSQL, contrat d'API, conventions |
| [`docs/conception/`](docs/conception/) | Diagramme de classes : [`Global.png`](docs/conception/Global.png) et un diagramme par module (`M1.png` à `M4.png`) |
| [`docs/plan/`](docs/plan/) | Plan de réalisation : qui fait quoi, phases, entraide |

## Règles de travail

- **Chacun modifie uniquement ses propres entités.** Pour utiliser celles d'un autre, on passe par son **Service**, jamais directement par ses entités.
- Le **contrat d'API** et le **diagramme de classes** ne changent qu'avec l'accord de tous.
- Les entités ne sont jamais exposées directement : on utilise toujours des **DTO**.
- Les règles métier sont dans les **Services**, pas dans les Controllers.
- Chaque endpoint est protégé avec `@PreAuthorize` selon les rôles.

## Git

- Branches : `main` (livrable), `develop` (intégration), `feature/m1-collectes`, `feature/m2-alertes`, `feature/m3-ressources`, `feature/m4-carte`
- On travaille sur sa branche `feature/...` et on fusionne vers `develop` par **pull request**.
- Relecture croisée : M1 relit M2, M2 relit M3, M3 relit M4, M4 relit M1.
- Messages de commit : `feat(m2): ajout du cycle de vie de l'alerte`, `fix(m1): ...`, `test(m3): ...`, `docs: ...`