# OliveConnect · application frontend commune

Application React, TypeScript et Vite pour les parcours M1, M2, M3 et M4. Le projet reste dans `frontend/m3/` pour conserver l’environnement déjà installé; l’interface est désormais commune aux quatre modules.

## Démarrer

```bash
npm install
npm run dev
```

Vite sert l’application sur `http://localhost:5173`. Le backend local est défini par `VITE_API_BASE_URL` dans `.env.local` et vaut `http://localhost:8099` par défaut.

## Architecture

- `src/app` : routes partagées.
- `src/auth` : session JWT, rôle décodé à titre indicatif et visibilité des fonctions.
- `src/api` : client HTTP central, types DTO, endpoints et services séparés par module.
- `src/components/layout` et `src/components/ui` : shell commun et composants visuels.
- `src/features/m1m2` : parcours préparés selon les documents; formulaires/actions désactivés tant que le backend manque.
- `src/features/m3` : CRUD, disponibilité, affectations, calendrier, stocks bas et import.
- `src/features/m4` : carte, indicateurs, pressoir et réceptions.
- `src/styles` : identité visuelle et adaptations responsive.

## Contrats et état d’intégration

M3 et M4 appellent les contrôleurs présents dans ce dépôt via `src/api/m3.ts` et `src/api/m4.ts`. Les réponses paginées suivent le format DTO défini côté serveur. Les erreurs sont présentées avec leur code HTTP.

Les services `src/api/m1.ts` et `src/api/m2.ts` sont préparés à partir des routes du cahier des charges. Aucun contrôleur M1/M2 n’est actuellement présent dans le backend; leurs pages affichent les formulaires, filtres, états de cycle de vie et tableaux prévus, avec actions désactivées et sans fausses données.

La connexion commune `/api/auth/login` est documentée mais n’est pas implémentée par le backend. Dans **Connexion sécurisée**, l’utilisateur peut fournir un JWT déjà émis par son environnement. Il est conservé localement dans le navigateur. Le rôle extrait du claim JWT ne sert qu’à adapter les menus; Spring Security garde le contrôle d’accès final.

Le formulaire de jeton de développement utilise l’endpoint réel `/api/dev/token` uniquement lorsque `VITE_ENABLE_DEV_TOKEN_LOGIN=true` et que le backend Spring démarre sous le profil `dev`. Cette option est désactivée par défaut dans `.env.example`.

## Fonctionnalités branchées

- M3 : listes paginées et CRUD de ressources, recherche par période, création/suppression des affectations, calendrier interne, seuils de stock bas, import multipart JSON/CSV et rapport des erreurs.
- M4 : carte publique ou complète (`/api/public/carte`, `/api/carte/vergers`), indicateurs protégés par rôle, gestion du pressoir et réceptions. Les fonds cartographiques utilisent les tuiles OpenStreetMap avec attribution; les marqueurs utilisent les coordonnées reçues de M4.
- Le port backend reste `8099`; aucune configuration Java n’est modifiée par ce frontend.

## Rôles

Les menus reflètent les rôles observés dans `@PreAuthorize` et les documents. Cette présentation ne remplace jamais les vérifications du serveur. Les écrans M1/M2 sont des préparations contractuelles, pas des opérations simulées.

## Vérification

```bash
npm run build
```

Le build TypeScript s’exécute avant le bundle Vite.
