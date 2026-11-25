/** Contrats M2 documentés; à activer seulement après livraison du backend M2. */
import { apiRequest } from './client';
import { endpoints } from './endpoints';

export const m2Api = {
  createAlert: (payload:unknown) => apiRequest<unknown>(endpoints.m2.alertes,{method:'POST',body:JSON.stringify(payload)}),
  alerts: (statut?:string) => apiRequest<unknown>(`${endpoints.m2.alertes}${statut?`?statut=${encodeURIComponent(statut)}`:''}`),
  myAlerts: () => apiRequest<unknown>(`${endpoints.m2.alertes}/mes-alertes`),
  nearbyAlerts: (lat:number,lng:number,rayon:number) => apiRequest<unknown>(`${endpoints.m2.alertes}/proches?lat=${lat}&lng=${lng}&rayon=${rayon}`),
  takeAlert: (id:string) => apiRequest<unknown>(`${endpoints.m2.alertes}/${encodeURIComponent(id)}/prendre-en-charge`,{method:'PATCH'}),
  resolveAlert: (id:string) => apiRequest<unknown>(`${endpoints.m2.alertes}/${encodeURIComponent(id)}/resoudre`,{method:'PATCH'}),
  convertAlertToHarvest: (id:string) => apiRequest<unknown>(`${endpoints.m2.alertes}/${encodeURIComponent(id)}/transformer-en-recolte`,{method:'PATCH'}),
  notifications: () => apiRequest<unknown>(endpoints.m2.notifications),
  markNotificationRead: (id:string) => apiRequest<unknown>(`${endpoints.m2.notifications}/${encodeURIComponent(id)}/lu`,{method:'PATCH'}),
};
