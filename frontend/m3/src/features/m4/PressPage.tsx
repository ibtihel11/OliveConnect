import { useEffect, useState, type FormEvent } from 'react';
import { m4Api } from '../../api/m4';
import type { Pressoir, QualiteReception, Reception } from '../../api/types';
import { useSession } from '../../auth/session';
import { Button, Card } from '../../components/ui/Primitives';
import { Empty, ErrorNotice, Loading, PageTitle, PermissionNotice, SuccessNotice } from '../common/states';

const qualities:QualiteReception[]=['EXCELLENTE','BONNE','MOYENNE','MAUVAISE'];
export function PressPage(){
  const {role,token}=useSession();
  const isPress=role==='RESPONSABLE_PRESSOIR';
  const [press,setPress]=useState<Pressoir>();
  const [receptions,setReceptions]=useState<Reception[]>([]);
  const [error,setError]=useState<unknown>();
  const [success,setSuccess]=useState('');
  const [loading,setLoading]=useState(true);
  const [busy,setBusy]=useState(false);
  const [pressForm,setPressForm]=useState({nom:'',adresse:'',capaciteTraitement:0});
  const [receptionForm,setReceptionForm]=useState({tourneeId:'',benneId:'',poids:0,qualite:'BONNE' as QualiteReception});

  useEffect(()=>{
    if(!token)return;
    setLoading(true);
    const pressRequest=isPress?m4Api.presses():Promise.resolve(null);
    Promise.allSettled([pressRequest,m4Api.receptions()]).then(([p,r])=>{
      if(p.status==='fulfilled'&&p.value){const found=p.value.content[0];setPress(found);if(found)setPressForm({nom:found.nom,adresse:found.adresse,capaciteTraitement:found.capaciteTraitement})}else if(p.status==='rejected')setError(p.reason);
      if(r.status==='fulfilled')setReceptions(r.value.content);else setError(r.reason);
    }).finally(()=>setLoading(false));
  },[token,isPress]);

  async function savePress(e:FormEvent){e.preventDefault();setBusy(true);setError(undefined);setSuccess('');try{const result=await m4Api.savePress(pressForm);setPress(result);setSuccess('Les informations du pressoir ont été enregistrées.')}catch(e){setError(e)}finally{setBusy(false)}}
  async function saveReception(e:FormEvent){
    e.preventDefault();if(!press){setError(new Error('Aucun pressoir n’est associé au compte connecté.'));return}
    setBusy(true);setError(undefined);setSuccess('');
    try{await m4Api.addReception({...receptionForm,pressoirId:press.id});setReceptions((await m4Api.receptions()).content);setReceptionForm({tourneeId:'',benneId:'',poids:0,qualite:'BONNE'});setSuccess('La réception a été enregistrée.')}catch(e){setError(e)}finally{setBusy(false)}
  }

  if(!token)return <PermissionNotice roles={['RESPONSABLE_PRESSOIR']}/>;
  return <div className="page-enter">
    <PageTitle kicker="TRANSFORMATION DES OLIVES" title="Pressoir & réceptions" description="Données et actions reliées aux contrôleurs M4. L’accès est vérifié par le serveur selon votre rôle."/>
    {error?<ErrorNotice error={error}/>:null}{success?<SuccessNotice>{success}</SuccessNotice>:null}
    <Card><div className="card-heading"><div><span className="eyebrow eyebrow-dark">MON PRESSOIR</span><h2>{press?.nom??(loading?'Chargement…':'Compte pressoir')}</h2></div></div>
      {loading?<Loading/>:isPress?<form className="form-grid" onSubmit={savePress}>
        <label className="field"><span>Nom</span><input className="field-input" required value={pressForm.nom} onChange={e=>setPressForm({...pressForm,nom:e.target.value})}/></label>
        <label className="field"><span>Adresse</span><input className="field-input" required value={pressForm.adresse} onChange={e=>setPressForm({...pressForm,adresse:e.target.value})}/></label>
        <label className="field"><span>Capacité de traitement</span><input type="number" min="1" required className="field-input" value={pressForm.capaciteTraitement||''} onChange={e=>setPressForm({...pressForm,capaciteTraitement:Number(e.target.value)})}/></label>
        <div className="form-actions"><Button disabled={busy}>{busy?'Enregistrement…':'Enregistrer le pressoir'}</Button></div>
      </form>:<p className="subtle-note">La gestion du pressoir et l’enregistrement des réceptions sont réservés au rôle RESPONSABLE_PRESSOIR.</p>}
    </Card>
    {isPress&&<Card><h2>Enregistrer une réception</h2><p className="subtle-note">Le pressoir est associé automatiquement au compte par le backend. La tournée et la benne doivent exister dans leurs modules.</p><form className="form-grid" onSubmit={saveReception}>
      <label className="field"><span>ID tournée</span><input className="field-input" required value={receptionForm.tourneeId} onChange={e=>setReceptionForm({...receptionForm,tourneeId:e.target.value})}/></label>
      <label className="field"><span>ID benne</span><input className="field-input" required value={receptionForm.benneId} onChange={e=>setReceptionForm({...receptionForm,benneId:e.target.value})}/></label>
      <label className="field"><span>Poids reçu (kg)</span><input type="number" min="0.01" step="0.01" required className="field-input" value={receptionForm.poids||''} onChange={e=>setReceptionForm({...receptionForm,poids:Number(e.target.value)})}/></label>
      <label className="field"><span>Qualité</span><select className="field-input" value={receptionForm.qualite} onChange={e=>setReceptionForm({...receptionForm,qualite:e.target.value as QualiteReception})}>{qualities.map(q=><option key={q}>{q}</option>)}</select></label>
      <div className="form-actions"><Button disabled={busy||!press}>{busy?'Enregistrement…':'Enregistrer la réception'}</Button></div>
    </form></Card>}
    <Card className="table-card"><h2>Réceptions récentes</h2>{loading?<Loading/>:receptions.length===0?<Empty/>:<div className="table-wrap"><table className="data-table"><thead><tr><th>Date</th><th>Tournée</th><th>Benne</th><th>Poids kg</th><th>Qualité</th></tr></thead><tbody>{receptions.map(r=><tr key={r.id}><td>{new Date(r.dateReception).toLocaleString()}</td><td><code>{r.tourneeId}</code></td><td><code>{r.benneId}</code></td><td>{r.poids}</td><td>{r.qualite}</td></tr>)}</tbody></table></div>}</Card>
  </div>;
}
