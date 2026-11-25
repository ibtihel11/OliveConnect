import { useEffect, useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { m4Api } from '../../api/m4';
import { useSession } from '../../auth/session';
import type { DelaisDashboard, RendementVerger, RemplissageBennes } from '../../api/types';
import { Card } from '../../components/ui/Primitives';
import { ErrorNotice, Loading, PageTitle } from '../common/states';

export function DashboardPage(){
  const {role}=useSession();
  const {pathname}=useLocation();
  const isHome=pathname==='/';
  const canRead=role==='RESPONSABLE_COLLECTE'||role==='ADMINISTRATEUR';
  const [yieldRows,setYield]=useState<RendementVerger[]>([]);
  const [delay,setDelay]=useState<DelaisDashboard>();
  const [bins,setBins]=useState<RemplissageBennes>();
  const [errors,setErrors]=useState<unknown[]>([]);
  const [loading,setLoading]=useState(false);
  useEffect(()=>{
    if(!canRead)return;
    setLoading(true);
    Promise.allSettled([m4Api.rendement(),m4Api.delays(),m4Api.bins()]).then(results=>{
      if(results[0].status==='fulfilled')setYield(results[0].value);
      if(results[1].status==='fulfilled')setDelay(results[1].value);
      if(results[2].status==='fulfilled')setBins(results[2].value);
      setErrors(results.filter(r=>r.status==='rejected').map(r=>r.status==='rejected'?r.reason:null));
    }).finally(()=>setLoading(false));
  },[canRead]);
  return <div className="dashboard-page page-enter">
    {isHome&&<>
    <div className="eyebrow eyebrow-dark"><span className="eyebrow-line"/>OLIVECONNECT · ESPACE DE TRAVAIL</div>
    <section className="hero-panel"><div className="hero-image"/><div className="hero-shade"/><div className="hero-content"><div className="hero-kicker">AGRICULTURE · COLLECTE · TRANSFORMATION</div><h1>La terre donne.<br/><em>Le collectif avance.</em></h1><p>Un espace commun pour suivre les récoltes, les ressources et les opérations de l’oliveraie.</p><Link className="button button-lime" to="/carte">Explorer les vergers <span>→</span></Link></div><div className="hero-note"><span>OLIVECONNECT</span><span>Du verger au pressoir</span></div></section>
    <div className="module-grid"><Link to="/vergers"><span>01 · M1</span><strong>Vergers & récoltes</strong><small>Parcelles, demandes et tournées</small></Link><Link to="/alertes"><span>02 · M2</span><strong>Alertes & notifications</strong><small>Maturité, météo, ravageurs</small></Link><Link to="/ressources/ouvriers"><span>03 · M3</span><strong>Ressources</strong><small>Équipe, matériel et affectations</small></Link><Link to="/carte"><span>04 · M4</span><strong>Observatoire & pressoir</strong><small>Carte, indicateurs, réceptions</small></Link></div>
    </>}
    {canRead?<>
      <PageTitle kicker="INDICATEURS M4" title="La saison en données" description="Mesures calculées par le backend M4 à partir des relevés et réceptions disponibles."/>
      {errors.map((e,i)=><ErrorNotice key={i} error={e}/>)}
      {loading?<Loading/>:<>
        <div className="metric-grid"><div><strong>{delay?.nombreRecoltes??'—'}</strong><span>Récoltes suivies</span></div><div><strong>{delay?.delaiMoyenJours??'—'}</strong><span>Délai moyen · jours</span></div><div><strong>{bins?.tauxRemplissageMoyenPourcent==null?'—':`${bins.tauxRemplissageMoyenPourcent.toFixed(1)}%`}</strong><span>Remplissage moyen des bennes</span></div></div>
        <Card className="table-card"><h2>Rendement par verger</h2>{yieldRows.length===0?<p className="subtle-note">Aucune mesure renvoyée actuellement.</p>:<div className="table-wrap"><table className="data-table"><thead><tr><th>Verger</th><th>Superficie ha</th><th>Récolté kg</th><th>Rendement kg/ha</th><th>Réceptions %</th><th>Tournées</th></tr></thead><tbody>{yieldRows.map(v=><tr key={v.vergerId}><td>{v.nomVerger}</td><td>{v.superficie??'—'}</td><td>{v.quantiteRecolteeKg??'—'}</td><td>{v.rendementKgParHa??'—'}</td><td>{v.tauxReceptionPourcent??'—'}</td><td>{v.nombreTournees}</td></tr>)}</tbody></table></div>}</Card>
        <Card className="table-card"><h2>Délais par verger</h2>{!delay||delay.parVerger.length===0?<p className="subtle-note">Aucun délai par verger renvoyé actuellement.</p>:<div className="table-wrap"><table className="data-table"><thead><tr><th>Verger</th><th>Récoltes</th><th>Délai moyen · jours</th></tr></thead><tbody>{delay.parVerger.map(v=><tr key={v.vergerId}><td>{v.nomVerger}</td><td>{v.nombreRecoltes}</td><td>{v.delaiMoyenJours??'—'}</td></tr>)}</tbody></table></div>}</Card>
        <Card className="table-card"><h2>Remplissage par benne</h2>{!bins||bins.parBenne.length===0?<p className="subtle-note">Aucune réception renvoyée actuellement.</p>:<div className="table-wrap"><table className="data-table"><thead><tr><th>Immatriculation</th><th>Capacité kg</th><th>Réceptions</th><th>Poids moyen kg</th><th>Taux moyen %</th><th>Maximum %</th></tr></thead><tbody>{bins.parBenne.map(v=><tr key={v.benneId}><td>{v.immatriculation}</td><td>{v.capaciteKg}</td><td>{v.nombreReceptions}</td><td>{v.poidsMoyenKg}</td><td>{v.tauxRemplissageMoyenPourcent}</td><td>{v.tauxRemplissageMaxPourcent}</td></tr>)}</tbody></table></div>}</Card>
      </>}
    </>:<Card className="quiet-note"><p>Les indicateurs consolidés M4 sont limités aux rôles RESPONSABLE_COLLECTE et ADMINISTRATEUR. Les fonctions accessibles selon votre rôle restent disponibles dans la navigation.</p></Card>}
  </div>;
}
