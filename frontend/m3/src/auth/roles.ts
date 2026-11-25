export type M3Role = 'RESPONSABLE_LOGISTIQUE' | 'RESPONSABLE_COLLECTE';

/** Autorisations reflétant les @PreAuthorize actuels; le backend reste l'autorité finale. */
export const roleAccess = {
  gestionRessources: ['RESPONSABLE_LOGISTIQUE'] as const,
  disponibilites: ['RESPONSABLE_LOGISTIQUE', 'RESPONSABLE_COLLECTE'] as const,
  affectationsLectureCreation: ['RESPONSABLE_LOGISTIQUE', 'RESPONSABLE_COLLECTE'] as const,
  suppressionAffectation: ['RESPONSABLE_LOGISTIQUE'] as const,
  calendrier: 'authenticated',
} as const;
