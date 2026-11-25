export interface Page<T> { content: T[]; page: number; size: number; totalElements: number }
export interface ApiErrorPayload { timestamp?: string; status?: number; error?: string; message?: string; path?: string }

export interface Ouvrier { id: string; nom: string; disponibilite: boolean; specialite: string; saisonnier: boolean }
export interface OuvrierInput { nom: string; disponibilite: boolean; specialite: string; saisonnier: boolean }
export interface Tracteur { id: string; nom: string; disponibilite: boolean; immatriculation: string; puissance: number }
export interface TracteurInput { nom: string; disponibilite: boolean; immatriculation: string; puissance: number }
export interface Benne { id: string; nom: string; disponibilite: boolean; capacite: number; immatriculation: string }
export interface BenneInput { nom: string; disponibilite: boolean; capacite: number; immatriculation: string }
export interface Materiel { id: string; nom: string; disponibilite: boolean; type: string; quantiteStock: number; seuilMinimum: number }
export interface MaterielInput { nom: string; disponibilite: boolean; type: string; quantiteStock: number; seuilMinimum: number }
export type ResourceRecord = Ouvrier | Tracteur | Benne | Materiel;
export type ResourceInput = OuvrierInput | TracteurInput | BenneInput | MaterielInput;
export type RessourceType = 'OUVRIER' | 'TRACTEUR' | 'BENNE' | 'MATERIEL';

export interface Affectation {
  id: string; ressourceId: string; ressourceType: RessourceType; tourneeId: string;
  dateDebut: string; dateFin: string; statut: string;
}
export interface AffectationInput {
  ressourceId: string; ressourceType: RessourceType; tourneeId: string;
  dateDebut: string; dateFin: string; statut: string;
}
export interface CalendrierAffectation { id: string; dateDebut: string; dateFin: string; statut: string }
export interface CalendrierRessource {
  id: string; nom: string; typeRessource: string; disponibilite: boolean;
  periodeDemandee: { debut: string; fin: string } | null; affectations: CalendrierAffectation[];
}
export interface ImportErreur { ligne: number; motif: string }
export interface ImportRapport { totalLignes: number; nombreImportes: number; nombreRejetes: number; erreurs: ImportErreur[] }

export interface VergerCartePublique { id: string; nom: string; latitude: number; longitude: number; niveauMaturite: number | null; typeOlives: string | null }
export interface ReleveRecent { date: string; niveauMaturite: number }
export interface MeteoRecente { dateReleve: string; temperature: number | null; humidite: number | null; precipitation: number | null }
export interface VergerCarte extends VergerCartePublique {
  adresse: string; superficie: number | null; statutVerger: string; statutRecolte: string | null;
  dateDernierReleve: string | null; derniersReleves: ReleveRecent[]; meteo: MeteoRecente | null;
}
export interface RendementVerger {
  vergerId: string; nomVerger: string; superficie: number | null; quantiteRecolteeKg: number | null;
  poidsRecuPressoirKg: number | null; rendementKgParHa: number | null; tauxReceptionPourcent: number | null; nombreTournees: number;
}
export interface DelaisDashboard {
  nombreRecoltes: number; delaiMoyenJours: number | null; delaiMinJours: number | null; delaiMaxJours: number | null;
  parType: { urgente: boolean; nombreRecoltes: number; delaiMoyenJours: number | null }[];
  parVerger: { vergerId: string; nomVerger: string; nombreRecoltes: number; delaiMoyenJours: number | null }[];
}
export interface RemplissageBennes {
  nombreReceptions: number; tauxRemplissageMoyenPourcent: number | null;
  parBenne: { benneId: string; immatriculation: string; capaciteKg: number; nombreReceptions: number; poidsMoyenKg: number; tauxRemplissageMoyenPourcent: number; tauxRemplissageMaxPourcent: number }[];
}
export interface Pressoir { id: string; nom: string; adresse: string; capaciteTraitement: number; responsableId: string }
export interface PressoirInput { nom: string; adresse: string; capaciteTraitement: number }
export type QualiteReception = 'EXCELLENTE' | 'BONNE' | 'MOYENNE' | 'MAUVAISE';
export interface Reception { id: string; tourneeId: string; benneId: string; pressoirId: string; dateReception: string; poids: number; qualite: QualiteReception }
export interface ReceptionInput { tourneeId: string; benneId: string; pressoirId: string; poids: number; qualite: QualiteReception }

// Types tirés du cadrage/UML M1 et M2. Le dépôt ne contient pas encore leur backend.
export type Role = 'AGRICULTEUR' | 'RESPONSABLE_COLLECTE' | 'CHEF_EQUIPE' | 'RESPONSABLE_LOGISTIQUE' | 'RESPONSABLE_PRESSOIR' | 'ADMINISTRATEUR';
export type StatutAlerte = 'NOUVELLE' | 'PRISE_EN_CHARGE' | 'RESOLUE';
export type TypeAlerte = 'MATURITE' | 'PLUIE' | 'RAVAGEURS';
export type StatutRecolte = 'EN_ATTENTE' | 'VALIDEE' | 'PLANIFIEE' | 'EN_COURS' | 'TERMINEE' | 'ANNULEE';
export type StatutTournee = 'PLANIFIEE' | 'EN_COURS' | 'TERMINEE' | 'ANNULEE';
