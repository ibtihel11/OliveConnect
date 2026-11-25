const apiBaseUrl = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8099').replace(/\/$/, '');

export type TokenProvider = () => string | null | undefined;

let tokenProvider: TokenProvider = () => null;

/** L'authentification appartient au socle commun; M3 reçoit seulement un fournisseur de JWT. */
export function configureTokenProvider(provider: TokenProvider) {
  tokenProvider = provider;
}

export class ApiError extends Error {
  constructor(
    message: string,
    readonly status: number,
    readonly payload?: unknown,
  ) {
    super(message);
    this.name = 'ApiError';
  }
}

export async function apiRequest<T>(path: string, init: RequestInit = {}): Promise<T> {
  const headers = new Headers(init.headers);
  if (init.body && !(init.body instanceof FormData) && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json');
  }

  const token = tokenProvider();
  if (token) headers.set('Authorization', `Bearer ${token}`);

  const response = await fetch(`${apiBaseUrl}${path}`, { ...init, headers });
  if (response.status === 204) return undefined as T;

  const contentType = response.headers.get('content-type') || '';
  const payload: unknown = contentType.includes('application/json') ? await response.json() : await response.text();
  if (!response.ok) {
    const message = typeof payload === 'object' && payload !== null && 'message' in payload
      ? String(payload.message)
      : `Erreur API (${response.status})`;
    throw new ApiError(message, response.status, payload);
  }
  return payload as T;
}

export function getApiBaseUrl() {
  return apiBaseUrl;
}
