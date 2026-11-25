const ressources = '/api/ressources';

export const endpoints = {
  // Contrôleurs M3 effectivement livrés.
  ouvriers: `${ressources}/ouvriers`,
  tracteurs: `${ressources}/tracteurs`,
  bennes: `${ressources}/bennes`,
  materiels: `${ressources}/materiels`,
  stockBas: `${ressources}/materiels/stock-bas`,
  importRessources: `${ressources}/import`,
  affectations: '/api/affectations',
  calendrier: '/api/calendrier',
  // Contrôleurs M4 effectivement livrés.
  cartePublique: '/api/public/carte',
  carteComplete: '/api/carte/vergers',
  rendement: '/api/dashboard/rendement',
  delais: '/api/dashboard/delais',
  remplissageBennes: '/api/dashboard/remplissage-bennes',
  pressoirs: '/api/pressoirs',
  receptions: '/api/receptions',
  devToken: '/api/dev/token',
  // Contrats documentés pour M1/M2, sans contrôleurs correspondants dans ce dépôt.
  m1: {
    login: '/api/auth/login', register: '/api/auth/register', me: '/api/utilisateurs/me', users: '/api/utilisateurs',
    vergers: '/api/vergers', recoltes: '/api/recoltes', tournees: '/api/tournees',
  },
  m2: { alertes: '/api/alertes', notifications: '/api/notifications' },
} as const;

export type ResourceKind = 'ouvriers' | 'tracteurs' | 'bennes' | 'materiels';
export const resourceKinds: ResourceKind[] = ['ouvriers', 'tracteurs', 'bennes', 'materiels'];
