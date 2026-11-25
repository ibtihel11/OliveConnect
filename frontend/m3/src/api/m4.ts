import { apiRequest } from './client';
import { endpoints } from './endpoints';
import type { DelaisDashboard, Page, Pressoir, PressoirInput, Reception, ReceptionInput, RendementVerger, RemplissageBennes, VergerCarte, VergerCartePublique } from './types';

export const m4Api = {
  publicMap: () => apiRequest<VergerCartePublique[]>(endpoints.cartePublique),
  map: () => apiRequest<VergerCarte[]>(endpoints.carteComplete),
  rendement: () => apiRequest<RendementVerger[]>(endpoints.rendement),
  delays: () => apiRequest<DelaisDashboard>(endpoints.delais),
  bins: () => apiRequest<RemplissageBennes>(endpoints.remplissageBennes),
  presses: (page = 0, size = 20) => apiRequest<Page<Pressoir>>(`${endpoints.pressoirs}?page=${page}&size=${size}`),
  savePress: (body: PressoirInput) => apiRequest<Pressoir>(endpoints.pressoirs, { method: 'POST', body: JSON.stringify(body) }),
  receptions: (page = 0, size = 20) => apiRequest<Page<Reception>>(`${endpoints.receptions}?page=${page}&size=${size}`),
  addReception: (body: ReceptionInput) => apiRequest<Reception>(endpoints.receptions, { method: 'POST', body: JSON.stringify(body) }),
};
