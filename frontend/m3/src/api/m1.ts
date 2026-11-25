/**
 * Contrats issus du cadrage M1. Ces méthodes restent non appelées tant que le
 * backend M1 ne fournit pas les contrôleurs et DTO correspondants.
 */
import { apiRequest } from './client';
import { endpoints } from './endpoints';

export const m1Api = {
  register: (payload: unknown) => apiRequest<unknown>(endpoints.m1.register,{method:'POST',body:JSON.stringify(payload)}),
  login: (payload: unknown) => apiRequest<unknown>(endpoints.m1.login,{method:'POST',body:JSON.stringify(payload)}),
  me: () => apiRequest<unknown>(endpoints.m1.me),
  updateMe: (payload: unknown) => apiRequest<unknown>(endpoints.m1.me,{method:'PUT',body:JSON.stringify(payload)}),
  vergers: () => apiRequest<unknown>(endpoints.m1.vergers),
  createVerger: (payload: unknown) => apiRequest<unknown>(endpoints.m1.vergers,{method:'POST',body:JSON.stringify(payload)}),
  verger: (id: string) => apiRequest<unknown>(`${endpoints.m1.vergers}/${encodeURIComponent(id)}`),
  updateVerger: (id: string,payload: unknown) => apiRequest<unknown>(`${endpoints.m1.vergers}/${encodeURIComponent(id)}`,{method:'PUT',body:JSON.stringify(payload)}),
  recoltes: (filters: {statut?:string;vergerId?:string}={}) => { const q=new URLSearchParams(); if(filters.statut)q.set('statut',filters.statut);if(filters.vergerId)q.set('vergerId',filters.vergerId);return apiRequest<unknown>(`${endpoints.m1.recoltes}${q.size?`?${q}`:''}`) },
  createRecolte: (payload: unknown) => apiRequest<unknown>(endpoints.m1.recoltes,{method:'POST',body:JSON.stringify(payload)}),
  updateRecolte: (id:string,payload:unknown) => apiRequest<unknown>(`${endpoints.m1.recoltes}/${encodeURIComponent(id)}`,{method:'PUT',body:JSON.stringify(payload)}),
  validateRecolte: (id:string) => apiRequest<unknown>(`${endpoints.m1.recoltes}/${encodeURIComponent(id)}/valider`,{method:'PATCH'}),
  cancelRecolte: (id:string) => apiRequest<unknown>(`${endpoints.m1.recoltes}/${encodeURIComponent(id)}/annuler`,{method:'PATCH'}),
  createTournee: (payload:unknown) => apiRequest<unknown>(endpoints.m1.tournees,{method:'POST',body:JSON.stringify(payload)}),
  tournee: (id:string) => apiRequest<unknown>(`${endpoints.m1.tournees}/${encodeURIComponent(id)}`),
  updateTourneeProgress: (id:string,payload:unknown) => apiRequest<unknown>(`${endpoints.m1.tournees}/${encodeURIComponent(id)}/avancement`,{method:'PATCH',body:JSON.stringify(payload)}),
};
