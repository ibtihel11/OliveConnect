import type { ReactNode } from 'react';
import { Link } from 'react-router-dom';
import { ApiError } from '../../api/client';
import { Card } from '../../components/ui/Primitives';
import { useSession } from '../../auth/session';
import type { Role } from '../../api/types';

export function PageTitle({ kicker, title, description, action }: { kicker: string; title: string; description: string; action?: ReactNode }) {
  return <div className="page-title"><div><div className="eyebrow eyebrow-dark"><span className="eyebrow-line" />{kicker}</div><h1>{title}</h1><p>{description}</p></div>{action}</div>;
}
export function ErrorNotice({ error }: { error: unknown }) {
  const message = error instanceof ApiError ? `${error.message} (HTTP ${error.status})` : error instanceof Error ? error.message : 'Une erreur inconnue est survenue.';
  return <div className="notice notice-error" role="alert"><strong>La requête n’a pas abouti.</strong><span>{message}</span><small>Vérifiez la connexion au backend et les droits du compte.</small></div>;
}
export function SuccessNotice({ children }: { children: ReactNode }) { return <div className="notice notice-success" role="status"><strong>Terminé</strong><span>{children}</span></div>; }
export function Loading() { return <div className="loading-line"><span /> Chargement des données réelles…</div>; }
export function Empty({ children = 'Aucun résultat renvoyé par le serveur.' }: { children?: ReactNode }) { return <div className="empty-panel">{children}</div>; }
export function PermissionNotice({ roles }: { roles: readonly Role[] }) {
  const session = useSession();
  return <Card className="permission-card"><span className="eyebrow eyebrow-dark">ACCÈS CONTRÔLÉ PAR LE BACKEND</span><h2>Connectez-vous avec un rôle autorisé</h2><p>Cette opération est réservée à : {roles.join(' ou ')}. Le rôle affiché dans l’interface sert uniquement à guider la navigation; le serveur vérifie toujours le JWT.</p><Link className="button button-primary" to="/connexion">Gérer le jeton de session</Link>{session.role && <small>Rôle détecté dans le jeton : {session.role}</small>}</Card>;
}
export function UnavailableModule({ module, detail, endpoints = [] }: { module: string; detail: string; endpoints?: string[] }) {
  return <div className="page-enter"><PageTitle kicker={`ESPACE ${module}`} title={module} description={detail} /><Card className="availability-card"><span className="availability-mark">i</span><div><span className="eyebrow eyebrow-dark">CONTRAT DOCUMENTÉ · SERVICE ABSENT</span><h2>Les écrans sont prêts, l’API n’est pas encore disponible.</h2><p>Le dépôt ne contient pas de contrôleurs ni de services {module}. Aucun exemple local ne sera présenté comme donnée réelle. Les actions restent désactivées jusqu’à l’implémentation du contrat serveur.</p>{endpoints.length > 0 && <div className="contract-chips">{endpoints.map(path => <code key={path}>{path}</code>)}</div>}</div></Card></div>;
}
