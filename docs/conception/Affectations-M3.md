# Affectations internes M3

## API

- `POST /api/affectations` crée une affectation; rôles `RESPONSABLE_LOGISTIQUE` et `RESPONSABLE_COLLECTE`.
- `GET /api/affectations?ressourceId={id}&ressourceType={type}&page=0&size=20` retourne une page pour une ressource et son type; mêmes rôles.
- `DELETE /api/affectations/{id}` supprime une affectation; rôle `RESPONSABLE_LOGISTIQUE`.

Le DTO de création utilise `ressourceType` (`OUVRIER`, `TRACTEUR`, `BENNE`, `MATERIEL`), `ressourceId`, `tourneeId`, `dateDebut`, `dateFin` et `statut`. `tourneeId` est une référence externe opaque. Il est conservé mais aucune recherche dans M1 n'est effectuée et aucune existence de tournée n'est affirmée.

## Détection des conflits

Les dates sont représentées par des intervalles semi-ouverts `[dateDebut, dateFin)`. Une affectation existante chevauche la nouvelle quand `dateDebutExistante < dateFinDemandee` et `dateFinExistante > dateDebutDemandee`; les périodes exactement adjacentes sont donc acceptées. La recherche inclut `ressourceId` et `ressourceType`, afin que le même ID dans deux collections soit traité comme deux ressources différentes.

Les statuts `ANNULEE` et `TERMINEE` ne bloquent pas le créneau et ne déclenchent pas de recherche de conflit pour une nouvelle affectation inactive. Tout autre statut, y compris un statut futur non reconnu, bloque les chevauchements par défaut. Une ressource doit exister dans sa collection M3 et avoir `disponibilite=true` pour créer une affectation.

L'index composé MongoDB `idx_affectations_ressource_periode` sur `affectations` conserve l'ordre `{ ressourceId: 1, ressourceType: 1, dateDebut: 1, dateFin: 1 }`. Son préfixe accélère les lectures et listes d'affectations d'une ressource donnée; il soutient aussi la détection des conflits, qui filtre d'abord par ressource et type puis vérifie les bornes de période. La recherche de disponibilité par type ne précise pas `ressourceId` et exploite donc moins bien cet index. Un index supplémentaire commençant par `ressourceType` pourrait être envisagé si des mesures de performance le justifient; aucun index supplémentaire n'est déclaré pour le moment.

## Calendrier

Le DTO du calendrier contient maintenant les affectations M3 correspondantes, filtrées si une période est demandée. Il expose leur identifiant, leurs dates et leur statut, sans inclure `tourneeId` afin de ne pas représenter la référence comme une tournée vérifiée. La vérification ou l'affichage des tournées réelles attend le contrat M1.
