import { createContext, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';
import { configureTokenProvider } from '../api/client';
import { apiRequest } from '../api/client';
import { endpoints } from '../api/endpoints';
import type { Role } from '../api/types';

type Session = { token: string; role: Role | null; subject: string | null };
type SessionContextValue = Session & { setToken: (value: string) => void; clear: () => void; createDevToken: (userId: string, role: Role) => Promise<void> };
const Context = createContext<SessionContextValue | null>(null);
const storageKey = 'oliveconnect.jwt';
const roles: Role[] = ['AGRICULTEUR','RESPONSABLE_COLLECTE','CHEF_EQUIPE','RESPONSABLE_LOGISTIQUE','RESPONSABLE_PRESSOIR','ADMINISTRATEUR'];

function decode(token: string): Pick<Session, 'role' | 'subject'> {
  try {
    const payload = JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')));
    const role = roles.includes(payload.role) ? payload.role as Role : null;
    return { role, subject: typeof payload.sub === 'string' ? payload.sub : null };
  } catch { return { role: null, subject: null }; }
}

export function SessionProvider({ children }: { children: ReactNode }) {
  const [token, setTokenState] = useState(() => localStorage.getItem(storageKey) ?? '');
  const info = useMemo(() => decode(token), [token]);
  useEffect(() => { configureTokenProvider(() => token || null); }, [token]);
  const setToken = (value: string) => { const clean = value.trim(); if (clean) localStorage.setItem(storageKey, clean); else localStorage.removeItem(storageKey); setTokenState(clean); };
  const clear = () => setToken('');
  const createDevToken = async (userId: string, role: Role) => {
    const result = await apiRequest<{ token: string }>(`${endpoints.devToken}?userId=${encodeURIComponent(userId)}&role=${encodeURIComponent(role)}`);
    if (!result?.token) throw new Error('Le serveur n’a pas retourné de jeton.');
    setToken(result.token);
  };
  return <Context.Provider value={{ token, ...info, setToken, clear, createDevToken }}>{children}</Context.Provider>;
}
export function useSession() { const value = useContext(Context); if (!value) throw new Error('SessionProvider manquant.'); return value; }
export function hasRole(actual: Role | null, allowed: readonly Role[]) { return actual !== null && allowed.includes(actual); }
