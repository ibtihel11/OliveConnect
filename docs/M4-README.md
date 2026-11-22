# OliveConnect — backend (module M4: map, dashboard, pressing plant)

Spring Boot 3.3 / Java 17 / MongoDB. Follows the cadrage (step 1): package layout `m4_carte/{controller,service,repository,entity,dto}`,
JSON contract (section 6), error format (6.1), conventions (section 8).

> **Verification status.** The sources were written without a Java compiler or Maven available in the authoring
> environment. Pipeline JSON and the Postman collection were syntax-checked, but `mvn test` and a run against MongoDB
> have **not** been executed. Run step 2 below first and fix any compile error you meet.

## 1. Run

```bash
docker compose up -d                 # MongoDB 7 on :27017
mvn test                             # unit + web-layer tests (no MongoDB needed)
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```
Profile `dev` seeds demo data (ids from the cadrage example: `t7`, `b3`, `p1`), starts the simulators
(sensors every 30 s, press capacity) and exposes `GET /api/dev/token?userId=..&role=..` to get a JWT without M1's login.

Import `postman/OliveConnect-M4.postman_collection.json`, run folder **0 - Tokens** first, then the rest.

## 2. Endpoints (identical to cadrage 6.5)

| Method | Path | Role |
|---|---|---|
| GET | `/api/public/carte` | public |
| GET | `/api/carte/vergers` | any authenticated user |
| GET | `/api/dashboard/rendement` · `/delais` · `/remplissage-bennes` | RESPONSABLE_COLLECTE, ADMINISTRATEUR |
| GET / POST | `/api/pressoirs` | RESPONSABLE_PRESSOIR |
| POST | `/api/receptions` | RESPONSABLE_PRESSOIR (400 negative weight, 404 unknown benne) |
| GET | `/api/receptions?tourneeId=&page=&size=` | any authenticated user |

## 3. Decisions to validate with the group

- **Units**: `superficie` in hectares, `poids`/`capacite`/`quantite*` in kg, `niveauMaturite` 0–100 %.
- **Rendement** (per verger): a tour's received weight is split across its non-cancelled récoltes proportionally to
  `quantiteRecoltee` (equally if none entered). `rendementKgParHa = poidsRecu / superficie`.
- **Délai de collecte**: days between `dateSouhaitee` and the tour's `dateDebut` (tours EN_COURS / TERMINEE).
- **Remplissage**: `poids / capacite de la benne × 100`, per benne and global (weighted).
- **POST /api/pressoirs** is create-or-update (relation 1–1 ResponsablePressoir–Pressoir): 201 first time, 200 afterwards.
  It is how the responsable "communicates the capacity" without adding an endpoint to the contract.
- **Extra business rules in POST /api/receptions** (not in the cadrage, easy to remove in `ReceptionService`):
  404 unknown pressoir/tournée, 403 pressoir managed by someone else, 409 tour not started (only EN_COURS/TERMINEE accepted).
- **Writes into M1's data**: only `vergers.derniersReleves` (5 latest `{date, niveauMaturite}`), as required by cadrage 4.1. Agree this with M1.
- **Reads of M1/M3 data** go through ports `TourneeReader` / `BenneReader` (read-only Mongo today; swap for `TourneeService` later)
  and read-only aggregations on `vergers`, `varietes`, `recoltes`, `tournees`, `bennes`.
  Joins compare ids as strings, so both ObjectId and String `_id` work. Dates are expected as BSON dates (Spring Data default).
- **JWT contract with M1**: HS256, `sub` = user id, claim `role` = Role enum name, same secret (`OLIVECONNECT_JWT_SECRET`).
  `common/security` is a shared skeleton to merge with M1's real security. `DevTokenController` is dev-only.
- Carte responses are cached in memory (TTL `oliveconnect.carte.cache-ttl-ms`, default 30 s).
- Extra indexes on M4 collections: unique sparse `pressoirs.responsableId`; `(vergerId, dateReleve desc)` on `meteo` and `releves_maturite`.

## 4. Layout
```
common/{config,security,exception,dto}   shared skeleton (errors, JWT, pagination, CORS)
m4_carte/entity       Pressoir, Reception, ReleveMaturite, DonneeMeteo, QualiteReception
m4_carte/repository   Spring Data + CarteRepository / DashboardRepository (aggregation pipelines)
m4_carte/service      Reception, Pressoir, Carte, Dashboard, MesureIngestion, Simulation
m4_carte/controller   Carte, Dashboard, Pressoir, Reception
m4_carte/seed         DevDataSeeder (dev only; mocks M1/M3 collections only when empty)
```
