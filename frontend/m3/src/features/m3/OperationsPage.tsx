import { useCallback, useEffect, useState, type FormEvent } from 'react';
import { useParams } from 'react-router-dom';
import { m3Api } from '../../api/m3';
import type { ResourceKind } from '../../api/endpoints';
import type { Affectation, CalendrierRessource, ImportRapport, Materiel, ResourceRecord, RessourceType } from '../../api/types';
import { useSession } from '../../auth/session';
import { Badge, Button, Card } from '../../components/ui/Primitives';
import { Empty, ErrorNotice, Loading, PageTitle, PermissionNotice, SuccessNotice } from '../common/states';

const labels:Record<string,string>={disponibilites:'Disponibilités',affectations:'Affectations',calendrier:'Calendrier',stocks:'Stocks bas',import:'Import de ressources'};
const resourceTypes:Record<string,RessourceType>={ouvriers:'OUVRIER',tracteurs:'TRACTEUR',bennes:'BENNE',materiels:'MATERIEL'};
const nowLocal=()=>new Date(Date.now()-new Date().getTimezoneOffset()*60000).toISOString().slice(0,16);
const tomorrowLocal=()=>new Date(Date.now()+86400000-new Date().getTimezoneOffset()*60000).toISOString().slice(0,16);

export function OperationsPage(){
  const {section='disponibilites'}=useParams();
  const {role,token}=useSession();
  const logistic=role==='RESPONSABLE_LOGISTIQUE';
  const logisticsOrCollection=logistic||role==='RESPONSABLE_COLLECTE';
  const logisticOnly=['stocks','import'].includes(section);
  const sectionAllowed=section==='calendrier'?Boolean(token):(logisticOnly?logistic:logisticsOrCollection);
  const [error,setError]=useState<unknown>();
  const [success,setSuccess]=useState('');
  const [busy,setBusy]=useState(false);
  const [listLoading,setListLoading]=useState(false);
  const [rows,setRows]=useState<ResourceRecord[]>([]);
  const [availablePage,setAvailablePage]=useState(0);
  const [availableTotal,setAvailableTotal]=useState(0);
  const [materials,setMaterials]=useState<Materiel[]>([]);
  const [assignments,setAssignments]=useState<Affectation[]>([]);
  const [assignPage,setAssignPage]=useState(0);
  const [assignTotal,setAssignTotal]=useState(0);
  const [calendar,setCalendar]=useState<CalendrierRessource>();
  const [report,setReport]=useState<ImportRapport>();
  const [kind,setKind]=useState<ResourceKind>('ouvriers');
  const [start,setStart]=useState(nowLocal);
  const [end,setEnd]=useState(tomorrowLocal);
  const [resourceId,setResourceId]=useState('');
  const [file,setFile]=useState<File>();
  const [assignment,setAssignment]=useState({ressourceId:'',ressourceType:'OUVRIER' as RessourceType,tourneeId:'',dateDebut:nowLocal(),dateFin:''});

  const refreshAssignments=useCallback(async(page=assignPage)=>{const result=await m3Api.assignments(page,20);setAssignments(result.content);setAssignTotal(result.totalElements)},[assignPage]);
  useEffect(()=>{
    setError(undefined);setSuccess('');setRows([]);setAssignments([]);setMaterials([]);setCalendar(undefined);setReport(undefined);
    let active=true;
    if(section==='stocks'&&token&&logistic){setListLoading(true);m3Api.lowStock().then(data=>{if(active)setMaterials(data)}).catch(e=>{if(active)setError(e)}).finally(()=>{if(active)setListLoading(false)})}
    else if(section==='affectations'&&token&&logisticsOrCollection){setListLoading(true);refreshAssignments(assignPage).catch(e=>setError(e)).finally(()=>{if(active)setListLoading(false)})}
    else setListLoading(false);
    return()=>{active=false};
  },[section,token,logistic,logisticsOrCollection,assignPage,refreshAssignments]);

  async function fetchAvailable(page:number){
    if(new Date(start)>=new Date(end)){setError(new Error('La date de début doit précéder la date de fin.'));return}
    setBusy(true);setError(undefined);setSuccess('');
    try{const result=await m3Api.available(kind,start?new Date(start).toISOString():'',end?new Date(end).toISOString():'',page,20);setRows(result.content);setAvailableTotal(result.totalElements);setAvailablePage(page)}
    catch(e){setError(e)}finally{setBusy(false)}
  }
  async function searchAvailable(e:FormEvent){e.preventDefault();await fetchAvailable(0)}
  async function submitAssignment(e:FormEvent){
    e.preventDefault();if(new Date(assignment.dateDebut)>=new Date(assignment.dateFin)){setError(new Error('La date de début doit précéder la date de fin.'));return}
    setBusy(true);setError(undefined);setSuccess('');
    try{await m3Api.createAssignment({...assignment,dateDebut:new Date(assignment.dateDebut).toISOString(),dateFin:new Date(assignment.dateFin).toISOString(),statut:'PLANIFIEE'});await refreshAssignments();setSuccess('Affectation créée. Le serveur a validé le créneau.')}
    catch(e){setError(e)}finally{setBusy(false)}
  }
  async function removeAssignment(item:Affectation){
    if(!window.confirm('Supprimer cette affectation ?'))return;
    try{await m3Api.removeAssignment(item.id);await refreshAssignments();setSuccess('Affectation supprimée.')}catch(e){setError(e)}
  }
  async function searchCalendar(e:FormEvent){e.preventDefault();if(Boolean(start)!==Boolean(end)){setError(new Error('Renseignez les deux dates de la période, ou laissez les deux vides.'));return}if(start&&end&&new Date(start)>=new Date(end)){setError(new Error('La date de début doit précéder la date de fin.'));return}setBusy(true);setError(undefined);try{setCalendar(await m3Api.calendar(resourceId,start?new Date(start).toISOString():undefined,end?new Date(end).toISOString():undefined))}catch(e){setError(e)}finally{setBusy(false)}}
  async function importFile(e:FormEvent){e.preventDefault();if(!file)return;setBusy(true);setError(undefined);try{setReport(await m3Api.import(file))}catch(e){setError(e)}finally{setBusy(false)}}

  if(!sectionAllowed)return <PermissionNotice roles={section==='calendrier'?['AGRICULTEUR','RESPONSABLE_COLLECTE','CHEF_EQUIPE','RESPONSABLE_LOGISTIQUE','RESPONSABLE_PRESSOIR','ADMINISTRATEUR']:logisticOnly?['RESPONSABLE_LOGISTIQUE']:['RESPONSABLE_LOGISTIQUE','RESPONSABLE_COLLECTE']}/>;
  return <div className="page-enter">
    <PageTitle kicker="PLANIFICATION & LOGISTIQUE" title={labels[section]??'Opérations'} description={section==='disponibilites'?'La disponibilité combine le champ enregistré et les affectations actives renvoyées par M3.':section==='calendrier'?'Consultez les affectations réellement enregistrées pour une ressource.':'Opérations exécutées par les services M3.'}/>
    {error?<ErrorNotice error={error}/>:null}{success?<SuccessNotice>{success}</SuccessNotice>:null}

    {section==='disponibilites'&&<>
      <Card><form className="filter-form" onSubmit={searchAvailable}>
        <label className="field"><span>Type de ressource</span><select className="field-input" value={kind} onChange={e=>setKind(e.target.value as ResourceKind)}><option value="ouvriers">Ouvriers</option><option value="tracteurs">Tracteurs</option><option value="bennes">Bennes</option><option value="materiels">Matériels</option></select></label>
        <label className="field"><span>Début</span><input required type="datetime-local" className="field-input" value={start} onChange={e=>setStart(e.target.value)}/></label>
        <label className="field"><span>Fin</span><input required type="datetime-local" className="field-input" value={end} onChange={e=>setEnd(e.target.value)}/></label>
        <Button disabled={busy}>{busy?'Recherche…':'Rechercher'}</Button>
      </form><p className="subtle-note">Période ISO 8601, intervalles semi-ouverts. Les conflits sont évalués côté serveur; aucune réservation n’est simulée ici.</p></Card>
      <Card className="table-card"><div className="card-heading"><h2>Ressources disponibles</h2><span className="badge badge-neutral">{availableTotal} résultat{availableTotal===1?'':'s'}</span></div>
        {busy?<Loading/>:rows.length===0?<Empty>Indiquez une période puis lancez la recherche.</Empty>:<div className="table-wrap"><table className="data-table"><thead><tr><th>Nom</th><th>Identifiant</th><th>Disponibilité</th><th>Détail</th></tr></thead><tbody>{rows.map(r=><tr key={r.id}><td>{r.nom}</td><td><code>{r.id}</code></td><td><Badge tone="positive">Disponible</Badge></td><td>{'immatriculation' in r?r.immatriculation:'type' in r?r.type:'specialite' in r?r.specialite:'—'}</td></tr>)}</tbody></table></div>}
        {availableTotal>20&&<div className="pagination"><button className="button button-secondary" disabled={availablePage===0||busy} onClick={()=>void fetchAvailable(availablePage-1)}>Précédent</button><span>Page {availablePage+1} sur {Math.ceil(availableTotal/20)}</span><button className="button button-secondary" disabled={(availablePage+1)*20>=availableTotal||busy} onClick={()=>void fetchAvailable(availablePage+1)}>Suivant</button></div>}
      </Card>
    </>}

    {section==='affectations'&&<>
      <Card><h2>Nouvelle affectation</h2><p className="subtle-note">La tournée M1 est saisie comme identifiant de référence; son existence est à confirmer avec le contrat M1. M3 vérifie la ressource et les conflits côté serveur.</p><form className="form-grid" onSubmit={submitAssignment}>
        <label className="field"><span>Type de ressource</span><select className="field-input" value={assignment.ressourceType} onChange={e=>setAssignment({...assignment,ressourceType:e.target.value as RessourceType})}>{Object.entries(resourceTypes).map(([key,value])=><option key={value} value={value}>{key}</option>)}</select></label>
        <label className="field"><span>ID ressource</span><input required className="field-input" value={assignment.ressourceId} onChange={e=>setAssignment({...assignment,ressourceId:e.target.value})}/></label>
        <label className="field"><span>ID tournée M1</span><input required className="field-input" value={assignment.tourneeId} onChange={e=>setAssignment({...assignment,tourneeId:e.target.value})}/></label>
        <label className="field"><span>Début</span><input required type="datetime-local" className="field-input" value={assignment.dateDebut} onChange={e=>setAssignment({...assignment,dateDebut:e.target.value})}/></label>
        <label className="field"><span>Fin</span><input required type="datetime-local" className="field-input" value={assignment.dateFin} onChange={e=>setAssignment({...assignment,dateFin:e.target.value})}/></label>
        <div className="form-actions"><Button disabled={busy}>{busy?'Création…':'Créer l’affectation'}</Button></div>
      </form></Card>
      <Card className="table-card"><div className="card-heading"><h2>Affectations enregistrées</h2><span className="badge badge-neutral">{assignTotal} au total</span></div>
        {listLoading?<Loading/>:assignments.length===0?<Empty/>:<div className="table-wrap"><table className="data-table"><thead><tr><th>Ressource</th><th>Type</th><th>Tournée</th><th>Période</th><th>Statut</th><th>Actions</th></tr></thead><tbody>{assignments.map(a=><tr key={a.id}><td><code>{a.ressourceId}</code></td><td>{a.ressourceType}</td><td><code>{a.tourneeId}</code></td><td>{new Date(a.dateDebut).toLocaleString()} – {new Date(a.dateFin).toLocaleString()}</td><td>{a.statut}</td><td>{logistic?<button className="danger-link" onClick={()=>void removeAssignment(a)}>Supprimer</button>:'—'}</td></tr>)}</tbody></table></div>}
        {assignTotal>20&&<div className="pagination"><button className="button button-secondary" disabled={assignPage===0||listLoading} onClick={()=>setAssignPage(assignPage-1)}>Précédent</button><span>Page {assignPage+1} sur {Math.ceil(assignTotal/20)}</span><button className="button button-secondary" disabled={(assignPage+1)*20>=assignTotal||listLoading} onClick={()=>setAssignPage(assignPage+1)}>Suivant</button></div>}
      </Card>
    </>}

    {section==='calendrier'&&<><Card><form className="filter-form" onSubmit={searchCalendar}><label className="field"><span>ID de ressource</span><input required className="field-input" value={resourceId} onChange={e=>setResourceId(e.target.value)}/></label><label className="field"><span>Début (facultatif)</span><input type="datetime-local" className="field-input" value={start} onChange={e=>setStart(e.target.value)}/></label><label className="field"><span>Fin (facultatif)</span><input type="datetime-local" className="field-input" value={end} onChange={e=>setEnd(e.target.value)}/></label><Button disabled={busy}>Consulter</Button></form><p className="subtle-note">Le calendrier affiche uniquement les affectations M3 réelles. Le serveur peut refuser un identifiant ambigu.</p></Card>{calendar&&<Card className="calendar-result"><div className="card-heading"><div><span className="eyebrow eyebrow-dark">{calendar.typeRessource}</span><h2>{calendar.nom}</h2></div><Badge tone={calendar.disponibilite?'positive':'negative'}>{calendar.disponibilite?'Disponible':'Indisponible'}</Badge></div>{calendar.affectations.length===0?<Empty>Aucune affectation renvoyée sur cette période.</Empty>:calendar.affectations.map(a=><div className="calendar-event" key={a.id}><span/><div><strong>{a.statut}</strong><small>{new Date(a.dateDebut).toLocaleString()} — {new Date(a.dateFin).toLocaleString()}</small></div></div>)}</Card>}</>}

    {section==='stocks'&&<Card className="table-card"><div className="card-heading"><div><span className="eyebrow eyebrow-dark">SEUIL ATTEINT OU DÉPASSÉ</span><h2>Matériels nécessitant un réapprovisionnement</h2></div></div>{listLoading?<Loading/>:materials.length===0?<Empty/>:<div className="table-wrap"><table className="data-table"><thead><tr><th>Matériel</th><th>Type</th><th>Stock</th><th>Seuil minimum</th></tr></thead><tbody>{materials.map(m=><tr key={m.id}><td>{m.nom}</td><td>{m.type}</td><td><Badge tone="warning">{m.quantiteStock}</Badge></td><td>{m.seuilMinimum}</td></tr>)}</tbody></table></div>}</Card>}

    {section==='import'&&<><Card><h2>Importer les ressources</h2><p>Formats JSON ou CSV selon le contrat M3. Les lignes invalides sont rapportées par le serveur et les ressources existantes ne sont pas remplacées.</p><form onSubmit={importFile} className="upload-form"><label className="field"><span>Fichier JSON ou CSV</span><input required type="file" accept=".json,.csv,application/json,text/csv" onChange={e=>setFile(e.target.files?.[0])}/></label><Button disabled={busy||!file}>{busy?'Import en cours…':'Envoyer le fichier'}</Button></form></Card>{report&&<Card><span className="eyebrow eyebrow-dark">RAPPORT FOURNI PAR M3</span><div className="metric-grid"><div><strong>{report.totalLignes}</strong><span>Lignes</span></div><div><strong>{report.nombreImportes}</strong><span>Importées</span></div><div><strong>{report.nombreRejetes}</strong><span>Rejetées</span></div></div>{report.erreurs.length>0&&<div className="table-wrap"><table className="data-table"><thead><tr><th>Ligne</th><th>Motif</th></tr></thead><tbody>{report.erreurs.map((e,i)=><tr key={`${e.ligne}-${i}`}><td>{e.ligne}</td><td>{e.motif}</td></tr>)}</tbody></table></div>}</Card>}</>}
  </div>;
}
