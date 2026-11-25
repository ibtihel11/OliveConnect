import { useCallback, useEffect, useState, type FormEvent } from 'react';
import { useParams } from 'react-router-dom';
import { m3Api, resourceLabels } from '../../api/m3';
import { resourceKinds, type ResourceKind } from '../../api/endpoints';
import type { ResourceInput, ResourceRecord } from '../../api/types';
import { useSession } from '../../auth/session';
import { Button, Card } from '../../components/ui/Primitives';
import { Empty, ErrorNotice, Loading, PageTitle, PermissionNotice, SuccessNotice } from '../common/states';

const fields: Record<ResourceKind, { key: string; label: string; type?: string }[]> = {
  ouvriers: [{key:'nom',label:'Nom'}, {key:'specialite',label:'Spécialité'}, {key:'saisonnier',label:'Saisonnier',type:'checkbox'}, {key:'disponibilite',label:'Disponible',type:'checkbox'}],
  tracteurs: [{key:'nom',label:'Nom'}, {key:'immatriculation',label:'Immatriculation'}, {key:'puissance',label:'Puissance',type:'number'}, {key:'disponibilite',label:'Disponible',type:'checkbox'}],
  bennes: [{key:'nom',label:'Nom'}, {key:'immatriculation',label:'Immatriculation'}, {key:'capacite',label:'Capacité',type:'number'}, {key:'disponibilite',label:'Disponible',type:'checkbox'}],
  materiels: [{key:'nom',label:'Nom'}, {key:'type',label:'Type'}, {key:'quantiteStock',label:'Quantité en stock',type:'number'}, {key:'seuilMinimum',label:'Seuil minimum',type:'number'}, {key:'disponibilite',label:'Disponible',type:'checkbox'}],
};
const initial: Record<string, string | number | boolean> = { nom:'', disponibilite:true, specialite:'', saisonnier:false, immatriculation:'', puissance:1, capacite:'', type:'', quantiteStock:0, seuilMinimum:0 };

export function ResourcesPage() {
  const { section = 'ouvriers' } = useParams();
  const kind = resourceKinds.includes(section as ResourceKind) ? section as ResourceKind : 'ouvriers';
  const { role, token } = useSession();
  const [rows, setRows] = useState<ResourceRecord[]>([]); const [loading, setLoading] = useState(true); const [error, setError] = useState<unknown>(); const [page,setPage]=useState(0); const [total,setTotal]=useState(0);
  const [formOpen, setFormOpen] = useState(false); const [editing, setEditing] = useState<ResourceRecord | null>(null); const [form, setForm] = useState({ ...initial }); const [saving, setSaving] = useState(false); const [success,setSuccess]=useState('');
  const load = useCallback(async () => { setLoading(true); setError(undefined); try { const data = await m3Api.list(kind,page); setRows(data.content);setTotal(data.totalElements); } catch (e) { setError(e); } finally { setLoading(false); } }, [kind,page]);
  useEffect(() => { if (token && role === 'RESPONSABLE_LOGISTIQUE') void load(); else setLoading(false); }, [load, token, role]);
  if (!token || role !== 'RESPONSABLE_LOGISTIQUE') return <PermissionNotice roles={['RESPONSABLE_LOGISTIQUE']} />;
  const openCreate = () => { setEditing(null); setForm({...initial}); setFormOpen(true); };
  const openEdit = (row: ResourceRecord) => { setEditing(row); setForm({...initial, ...row} as typeof initial); setFormOpen(true); };
  async function submit(event: FormEvent) { event.preventDefault(); setSaving(true); setError(undefined);setSuccess(''); const payload: Record<string, unknown> = {}; for (const f of fields[kind]) { const value = form[f.key]; payload[f.key] = f.type === 'number' ? Number(value) : value; } try { if(editing) await m3Api.update(kind, editing.id, payload as unknown as ResourceInput); else await m3Api.create(kind, payload as unknown as ResourceInput); setSuccess(editing?'Ressource mise à jour.':'Ressource créée.');setFormOpen(false); await load(); } catch(e) { setError(e); } finally { setSaving(false); } }
  async function remove(row: ResourceRecord) { if(!window.confirm(`Supprimer « ${row.nom} » ?`)) return;try { await m3Api.remove(kind,row.id);setSuccess('Ressource supprimée.'); await load(); } catch(e) { setError(e); } }
  return <div className="page-enter"><PageTitle kicker="GESTION DES RESSOURCES" title={resourceLabels[kind]} description="Données chargées depuis les collections du module M3." action={<Button onClick={openCreate}>Ajouter une ressource</Button>} />
    {error ? <ErrorNotice error={error} /> : null}{success?<SuccessNotice>{success}</SuccessNotice>:null}
    {formOpen && <Card className="form-card"><div className="card-heading"><div><span className="eyebrow eyebrow-dark">{editing ? 'MODIFICATION' : 'NOUVELLE FICHE'}</span><h2>{editing ? `Modifier ${editing.nom}` : `Ajouter — ${resourceLabels[kind]}`}</h2></div><Button variant="quiet" onClick={()=>setFormOpen(false)}>Fermer</Button></div><form className="form-grid" onSubmit={submit}>{fields[kind].map(f => <label className={`field ${f.type==='checkbox'?'field-check':''}`} key={f.key}>{f.type==='checkbox'?<><input type="checkbox" checked={Boolean(form[f.key])} onChange={e=>setForm({...form,[f.key]:e.target.checked})}/><span>{f.label}</span></>:<><span>{f.label}</span><input required type={f.type==='number'?'number':'text'} min={f.type==='number'?0:undefined} step={f.key==='puissance'?1:undefined} className="field-input" value={form[f.key] as string|number} onChange={e=>setForm({...form,[f.key]:f.type==='number'?e.target.value:e.target.value})}/></>}</label>)}<div className="form-actions"><Button disabled={saving}>{saving?'Enregistrement…':'Enregistrer'}</Button></div></form></Card>}
    <Card className="table-card"><div className="card-heading"><div><span className="eyebrow eyebrow-dark">COLLECTION M3</span><h2>{loading?'Lecture en cours':`${total} ressource${total===1?'':'s'} au total`}</h2></div><span className="badge badge-neutral">Page {page+1}</span></div>{loading?<Loading/>:error?null:rows.length===0?<Empty/>:<div className="table-wrap"><table className="data-table"><thead><tr><th>Nom</th>{fields[kind].filter(f=>f.key!=='nom').map(f=><th key={f.key}>{f.label}</th>)}<th>Identifiant</th><th>Actions</th></tr></thead><tbody>{rows.map(row=><tr key={row.id}><td><strong>{row.nom}</strong></td>{fields[kind].filter(f=>f.key!=='nom').map(f=><td key={f.key}>{f.type==='checkbox'?(row as unknown as Record<string,unknown>)[f.key]?'Oui':'Non':String((row as unknown as Record<string,unknown>)[f.key]??'—')}</td>)}<td><code>{row.id}</code></td><td className="row-actions"><button onClick={()=>openEdit(row)}>Modifier</button><button className="danger-link" onClick={()=>void remove(row)}>Supprimer</button></td></tr>)}</tbody></table></div>}<div className="pagination"><button className="button button-secondary" disabled={page===0||loading} onClick={()=>setPage(page-1)}>Précédent</button><span>{rows.length} affichées · {total} au total</span><button className="button button-secondary" disabled={(page+1)*20>=total||loading} onClick={()=>setPage(page+1)}>Suivant</button></div></Card>
  </div>;
}
