import { apiRequest } from './client';
import { endpoints, type ResourceKind } from './endpoints';
import type { Affectation, AffectationInput, CalendrierRessource, ImportRapport, Materiel, Ouvrier, Page, ResourceInput, ResourceRecord, Tracteur, Benne } from './types';

const resourceUrl: Record<ResourceKind, string> = {
  ouvriers: endpoints.ouvriers, tracteurs: endpoints.tracteurs, bennes: endpoints.bennes, materiels: endpoints.materiels,
};
export const resourceLabels: Record<ResourceKind, string> = { ouvriers: 'Ouvriers', tracteurs: 'Tracteurs', bennes: 'Bennes', materiels: 'Matériels' };
export type ResourcePage = Page<ResourceRecord>;

export const m3Api = {
  list: (kind: ResourceKind, page = 0, size = 20) => apiRequest<Page<ResourceRecord>>(`${resourceUrl[kind]}?page=${page}&size=${size}`),
  create: (kind: ResourceKind, body: ResourceInput) => apiRequest<ResourceRecord>(resourceUrl[kind], { method: 'POST', body: JSON.stringify(body) }),
  update: (kind: ResourceKind, id: string, body: ResourceInput) => apiRequest<ResourceRecord>(`${resourceUrl[kind]}/${encodeURIComponent(id)}`, { method: 'PUT', body: JSON.stringify(body) }),
  remove: (kind: ResourceKind, id: string) => apiRequest<void>(`${resourceUrl[kind]}/${encodeURIComponent(id)}`, { method: 'DELETE' }),
  available: (kind: ResourceKind, debut: string, fin: string, page = 0, size = 20) => apiRequest<Page<ResourceRecord>>(`${resourceUrl[kind]}/disponibles?debut=${encodeURIComponent(debut)}&fin=${encodeURIComponent(fin)}&page=${page}&size=${size}`),
  lowStock: () => apiRequest<Materiel[]>(endpoints.stockBas),
  assignments: (page = 0, size = 20) => apiRequest<Page<Affectation>>(`${endpoints.affectations}?page=${page}&size=${size}`),
  createAssignment: (body: AffectationInput) => apiRequest<Affectation>(endpoints.affectations, { method: 'POST', body: JSON.stringify(body) }),
  removeAssignment: (id: string) => apiRequest<void>(`${endpoints.affectations}/${encodeURIComponent(id)}`, { method: 'DELETE' }),
  calendar: (ressourceId: string, debut?: string, fin?: string) => {
    const params = new URLSearchParams({ ressourceId });
    if (debut) params.set('debut', debut);
    if (fin) params.set('fin', fin);
    return apiRequest<CalendrierRessource>(`${endpoints.calendrier}?${params}`);
  },
  import: (file: File) => { const body = new FormData(); body.append('file', file); return apiRequest<ImportRapport>(endpoints.importRessources, { method: 'POST', body }); },
};

export type M3ResourceUnion = Ouvrier | Tracteur | Benne | Materiel;
