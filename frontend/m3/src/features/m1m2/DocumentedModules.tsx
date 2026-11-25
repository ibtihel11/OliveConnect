import { useParams } from 'react-router-dom';
import { Button, Card } from '../../components/ui/Primitives';
import { useSession } from '../../auth/session';
import type { Role } from '../../api/types';
import { PageTitle, PermissionNotice } from '../common/states';

const pages={
  vergers:{title:'Vergers & parcelles',module:'M1',description:'Exploitations rattachées à l’agriculteur connecté.',columns:['Nom','Adresse','Superficie (ha)','Statut','Parcelles'],form:[['Nom','text'],['Adresse','text'],['Superficie (ha)','number'],['Latitude','number'],['Longitude','number']]},
  recoltes:{title:'Demandes de récolte',module:'M1',description:'Demandes normales ou urgentes, de la validation à la fin de collecte.',columns:['Verger','Date souhaitée','Quantité estimée (kg)','Urgente','Statut'],form:[['Identifiant verger','text'],['Date souhaitée','date'],['Quantité estimée (kg)','number']]},
  tournees:{title:'Tournées de collecte',module:'M1',description:'Planification et avancement des tournées par la collecte et les chefs d’équipe.',columns:['Début','Fin','Responsable collecte','Chef d’équipe','Récoltes','Statut'],form:[['Date de début','datetime-local'],['Date de fin','datetime-local'],['Identifiant responsable collecte','text'],['Identifiant chef d’équipe','text'],['Identifiants récoltes','text']]},
  alertes:{title:'Alertes agricoles',module:'M2',description:'Alertes de maturité, météo et ravageurs selon leur état de traitement.',columns:['Création','Type','Verger','Description','Statut'],form:[['Type','select'],['Identifiant verger','text'],['Description','text'],['Latitude','number'],['Longitude','number']]},
  notifications:{title:'Notifications',module:'M2',description:'Notifications adressées à l’utilisateur et état de lecture.',columns:['Date','Titre','Message','Lecture'],form:[]},
} as const;
const paths:Record<string,string[]>={
  vergers:['GET /api/vergers','POST /api/vergers','PUT /api/vergers/{id}'],
  recoltes:['GET /api/recoltes?statut=&vergerId=','POST /api/recoltes','PUT /api/recoltes/{id}','PATCH /api/recoltes/{id}/valider','PATCH /api/recoltes/{id}/annuler'],
  tournees:['POST /api/tournees','GET /api/tournees/{id}','PATCH /api/tournees/{id}/avancement'],
  alertes:['GET /api/alertes?statut=','POST /api/alertes','PATCH /api/alertes/{id}/prendre-en-charge','PATCH /api/alertes/{id}/resoudre','PATCH /api/alertes/{id}/transformer-en-recolte'],
  notifications:['GET /api/notifications','PATCH /api/notifications/{id}/lu'],
};
const access:Record<string,Role[]>={
  vergers:['AGRICULTEUR','RESPONSABLE_COLLECTE','ADMINISTRATEUR'],
  recoltes:['AGRICULTEUR','RESPONSABLE_COLLECTE','CHEF_EQUIPE','ADMINISTRATEUR'],
  tournees:['RESPONSABLE_COLLECTE','CHEF_EQUIPE','ADMINISTRATEUR'],
  alertes:['AGRICULTEUR','RESPONSABLE_COLLECTE','CHEF_EQUIPE','ADMINISTRATEUR'],
  notifications:['AGRICULTEUR','RESPONSABLE_COLLECTE','CHEF_EQUIPE','RESPONSABLE_LOGISTIQUE','RESPONSABLE_PRESSOIR','ADMINISTRATEUR'],
};

export function DocumentedModulePage(){
  const {section='vergers'}=useParams();
  const {role,token}=useSession();
  const page=pages[section as keyof typeof pages]??pages.vergers;
  if(!token||!access[section]?.includes(role as Role))return <PermissionNotice roles={access[section]??['AGRICULTEUR']}/>;
  return <div className="page-enter">
    <PageTitle kicker={`${page.module} · ${page.module==='M1'?'GESTION DES COLLECTES':'ALERTES & NOTIFICATIONS'}`} title={page.title} description={page.description}/>
    <div className="integration-banner"><span className="integration-led"/><div><strong>Interface préparée · API en attente</strong><p>Les documents décrivent ces contrats, mais aucun contrôleur M1/M2 n’est présent dans ce dépôt. Ces champs et actions sont désactivés : rien n’est simulé ni envoyé.</p></div></div>
    {page.form.length>0&&<Card className="form-card"><div className="card-heading"><div><span className="eyebrow eyebrow-dark">{section==='vergers'?'FICHE EXPLOITATION':section==='recoltes'?'NOUVELLE DEMANDE':section==='tournees'?'PLANIFICATION':'SIGNALER UN ÉVÉNEMENT'}</span><h2>{section==='alertes'?'Créer une alerte':section==='tournees'?'Composer une tournée':section==='recoltes'?'Préparer une récolte':'Informations du verger'}</h2></div><span className="badge badge-neutral">API hors ligne</span></div>
      <form className="form-grid" onSubmit={e=>e.preventDefault()}>{page.form.map(([label,type])=><label className={`field ${label==='Description'?'field-wide':''}`} key={label}><span>{label}</span>{type==='select'?<select disabled className="field-input"><option>Choisir un type d’alerte</option><option>MATURITE</option><option>PLUIE</option><option>RAVAGEURS</option></select>:<input disabled type={type} className="field-input" placeholder={label==='Identifiants récoltes'?'Séparés par des virgules':''}/>}</label>)}
        {section==='vergers'&&<label className="field"><span>Statut du verger</span><select disabled className="field-input"><option>EN_ATTENTE</option><option>EN_RECOLTE</option><option>RECOLTE_TERMINEE</option></select></label>}
        {section==='recoltes'&&<label className="field field-check"><input type="checkbox" disabled/><span>Demande urgente</span></label>}
        {section==='tournees'&&<label className="field"><span>Statut initial</span><select disabled className="field-input"><option>PLANIFIEE</option></select></label>}
        <div className="form-actions"><Button disabled>{section==='alertes'?'Créer l’alerte':section==='tournees'?'Créer la tournée':section==='recoltes'?'Créer la demande':'Enregistrer le verger'}</Button></div>
      </form>
    </Card>}
    <ModuleDetail section={section}/>
    <Card className="table-card"><div className="card-heading"><div><span className="eyebrow eyebrow-dark">SUIVI</span><h2>{page.title}</h2></div>{section!=='notifications'&&<label className="field compact-filter"><span>Filtrer par état</span><select disabled className="field-input"><option>Tous les états</option>{(section==='alertes'?['NOUVELLE','PRISE_EN_CHARGE','RESOLUE']:['EN_ATTENTE','VALIDEE','PLANIFIEE','EN_COURS','TERMINEE','ANNULEE']).map(s=><option key={s}>{s}</option>)}</select></label>}</div>
      <div className="table-wrap"><table className="data-table"><thead><tr>{page.columns.map(c=><th key={c}>{c}</th>)}<th>Actions</th></tr></thead><tbody><tr><td colSpan={page.columns.length+1} className="empty-cell">{section==='notifications'?'Aucune notification retournée par M2.':'Aucun enregistrement retourné par le service.'}</td></tr></tbody></table></div>
      <div className="pending-action-row">{(section==='alertes'?['Prendre en charge','Résoudre','Transformer en récolte']:section==='recoltes'?['Valider','Annuler']:section==='notifications'?['Marquer comme lue']:section==='tournees'?['Voir le détail','Mettre à jour l’avancement']:['Ouvrir le détail','Modifier']).map(action=><Button key={action} variant="secondary" disabled>{action}</Button>)}</div>
    </Card>
    {section==='recoltes'&&<Card className="workflow-card"><span className="eyebrow eyebrow-dark">CYCLE DOCUMENTÉ</span><div className="workflow-steps">{['EN_ATTENTE','VALIDEE','PLANIFIEE','EN_COURS','TERMINEE'].map((status,i)=><div key={status}><span>{String(i+1).padStart(2,'0')}</span><strong>{status}</strong></div>)}</div><p className="subtle-note">Une demande peut aussi être ANNULEE conformément au contrat M1.</p><HistoryTable title="Historique des statuts"/></Card>}
    {section==='alertes'&&<Card className="workflow-card"><span className="eyebrow eyebrow-dark">TRAITEMENT DES ALERTES</span><div className="workflow-steps workflow-steps-three">{['NOUVELLE','PRISE_EN_CHARGE','RESOLUE'].map((status,i)=><div key={status}><span>{String(i+1).padStart(2,'0')}</span><strong>{status}</strong></div>)}</div><p className="subtle-note">Types documentés : MATURITE · PLUIE · RAVAGEURS. Les transitions attendent l’API M2.</p><HistoryTable title="Historique de traitement"/></Card>}
    <Card className="contract-card"><span className="eyebrow eyebrow-dark">CONTRATS DOCUMENTÉS</span><div className="contract-chips">{(paths[section]??[]).map(path=><code key={path}>{path}</code>)}</div></Card>
  </div>;
}

function ModuleDetail({section}:{section:string}){
  if(section==='vergers')return <Card className="workflow-card"><div className="card-heading"><div><span className="eyebrow eyebrow-dark">PARCELLES DU VERGER</span><h2>Composition parcellaire</h2></div><span className="badge badge-neutral">API hors ligne</span></div><p className="subtle-note">Le modèle UML rattache des parcelles au verger et associe une variété d’olives à chaque parcelle.</p><div className="form-grid"><label className="field"><span>Nom de parcelle</span><input disabled className="field-input"/></label><label className="field"><span>Superficie (ha)</span><input disabled type="number" className="field-input"/></label><label className="field"><span>Identifiant de variété</span><input disabled className="field-input"/></label><div className="form-actions"><Button disabled>Ajouter une parcelle</Button></div></div><div className="table-wrap"><table className="data-table"><thead><tr><th>Parcelle</th><th>Superficie (ha)</th><th>Variété</th></tr></thead><tbody><tr><td colSpan={3} className="empty-cell">Aucune parcelle retournée par M1.</td></tr></tbody></table></div></Card>;
  if(section==='tournees')return <Card className="workflow-card"><div className="card-heading"><div><span className="eyebrow eyebrow-dark">SUIVI TERRAIN</span><h2>Avancement de la tournée</h2></div></div><p className="subtle-note">Parcours du chef d’équipe; les mises à jour attendent le DTO M1.</p><div className="form-grid"><label className="field"><span>Quantité récoltée (kg)</span><input disabled type="number" className="field-input"/></label><label className="field"><span>Statut du verger</span><select disabled className="field-input"><option>EN_ATTENTE</option><option>EN_RECOLTE</option><option>RECOLTE_TERMINEE</option></select></label><div className="form-actions"><Button disabled>Enregistrer l’avancement</Button></div></div></Card>;
  return null;
}
function HistoryTable({title}:{title:string}){return <div className="history-table"><h3>{title}</h3><div className="table-wrap"><table className="data-table"><thead><tr><th>Date</th><th>Statut</th><th>Auteur</th></tr></thead><tbody><tr><td colSpan={3} className="empty-cell">L’historique apparaîtra avec les données du backend.</td></tr></tbody></table></div></div>}
