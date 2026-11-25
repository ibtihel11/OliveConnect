import { useEffect, useMemo, useState } from 'react';
import { m4Api } from '../../api/m4';
import type { VergerCarte, VergerCartePublique } from '../../api/types';
import { useSession } from '../../auth/session';
import { Card } from '../../components/ui/Primitives';
import { Empty, ErrorNotice, Loading, PageTitle } from '../common/states';

type Verger = VergerCartePublique | VergerCarte;

export function MapPage(){
  const {token}=useSession();
  const [items,setItems]=useState<Verger[]>([]);
  const [error,setError]=useState<unknown>();
  const [loading,setLoading]=useState(true);
  const [zoom,setZoom]=useState(8);
  const [selected,setSelected]=useState<string|null>(null);
  const [query,setQuery]=useState('');
  const [oliveType,setOliveType]=useState('');
  const [maturity,setMaturity]=useState('all');
  useEffect(()=>{setLoading(true);(token?m4Api.map():m4Api.publicMap()).then(setItems).catch(setError).finally(()=>setLoading(false))},[token]);
  const filtered=useMemo(()=>items.filter(v=>v.nom.toLocaleLowerCase().includes(query.trim().toLocaleLowerCase())&&(!oliveType||v.typeOlives===oliveType)&&(maturity==='all'||(maturity==='known'?v.niveauMaturite!==null:v.niveauMaturite===null))),[items,query,oliveType,maturity]);
  const types=useMemo(()=>Array.from(new Set(items.map(v=>v.typeOlives).filter((v):v is string=>Boolean(v)))),[items]);
  const geo=useMemo(()=>{const n=2**zoom;const toTile=(lat:number,lon:number)=>({x:(lon+180)/360*n,y:(1-Math.asinh(Math.tan(lat*Math.PI/180))/Math.PI)/2*n});const coords=filtered.map(v=>({item:v,...toTile(v.latitude,v.longitude)}));const centerX=coords.length?coords.reduce((s,p)=>s+p.x,0)/coords.length:0;const centerY=coords.length?coords.reduce((s,p)=>s+p.y,0)/coords.length:0;const points=(coords.map(p=>({...p.item,x:500+(p.x-centerX)*256,y:350+(p.y-centerY)*256}))) as (Verger & {x:number;y:number})[];const tiles=[] as {x:number;y:number;left:number;top:number}[];for(let x=Math.floor(centerX)-2;x<=Math.floor(centerX)+2;x++)for(let y=Math.floor(centerY)-2;y<=Math.floor(centerY)+2;y++)tiles.push({x,y,left:500+(x-centerX)*256,top:350+(y-centerY)*256});return{points,tiles}},[filtered,zoom]);
  const points=geo.points;
  const point=points.find(v=>v.id===selected);
  return <div className="page-enter">
    <PageTitle kicker="OBSERVATION DU TERRITOIRE" title="Carte des vergers" description={token?'Vue complète des vergers et derniers relevés M4.':'Vue publique : localisation, maturité et type d’olives.'}/>
    {error?<ErrorNotice error={error}/>:null}
    <Card className="map-card">
      <div className="map-filters">
        <label className="field"><span>Rechercher un verger</span><input className="field-input" value={query} onChange={e=>setQuery(e.target.value)} placeholder="Nom du verger"/></label>
        <label className="field"><span>Type d’olives</span><select className="field-input" value={oliveType} onChange={e=>setOliveType(e.target.value)}><option value="">Tous les types</option>{types.map(type=><option key={type}>{type}</option>)}</select></label>
        <label className="field"><span>Maturité</span><select className="field-input" value={maturity} onChange={e=>setMaturity(e.target.value)}><option value="all">Tous</option><option value="known">Relevée</option><option value="missing">Sans relevé</option></select></label>
      </div>
      <div className="map-toolbar"><span>{filtered.length} résultat{filtered.length===1?'':'s'} · positions renvoyées par M4</span><div><button onClick={()=>setZoom(Math.max(4,zoom-1))} aria-label="Dézoomer">−</button><span>Niveau {zoom}</span><button onClick={()=>setZoom(Math.min(16,zoom+1))} aria-label="Zoomer">+</button></div></div>
      {loading?<Loading/>:points.length===0?<Empty>Aucun verger ne correspond aux filtres.</Empty>:<div className="map-viewport">
        <svg className="orchard-map" viewBox="0 0 1000 700" role="img" aria-label="Carte interactive des vergers OliveConnect">
          <rect width="1000" height="700" fill="#e5eadb"/>{geo.tiles.map(tile=><image key={`${tile.x}-${tile.y}`} x={tile.left} y={tile.top} width="256" height="256" href={`https://tile.openstreetmap.org/${zoom}/${tile.x}/${tile.y}.png`} preserveAspectRatio="none"/>)}
          {points.map(p=><g key={p.id} className={`map-point ${selected===p.id?'selected':''}`} onClick={()=>setSelected(selected===p.id?null:p.id)} role="button" tabIndex={0} onKeyDown={e=>e.key==='Enter'&&setSelected(p.id)}><circle cx={p.x} cy={p.y} r="17" className="map-point-halo"/><circle cx={p.x} cy={p.y} r="8" className="map-point-dot"/><circle cx={p.x} cy={p.y} r="3" fill="#fff"/><title>{p.nom} · {p.latitude.toFixed(4)}, {p.longitude.toFixed(4)}</title></g>)}
        </svg>
        {point&&<MapPopup point={point} onClose={()=>setSelected(null)}/>}
      </div>}
    </Card>
    <p className="subtle-note">Carte OpenStreetMap avec coordonnées réelles renvoyées par M4. Les filtres portent sur les mêmes champs API; aucun verger de démonstration n’est ajouté. © OpenStreetMap contributors.</p>
  </div>;
}

function MapPopup({point,onClose}:{point:Verger;onClose:()=>void}){
  const full='statutRecolte' in point;
  return <div className="map-popup"><button onClick={onClose} aria-label="Fermer">×</button><strong>{point.nom}</strong><span>{point.typeOlives??'Type d’olives non renseigné'} · Maturité {point.niveauMaturite??'—'}</span>
    {full&&<><span>Récolte : {point.statutRecolte??'—'} · {point.statutVerger}</span>{point.dateDernierReleve&&<span>Dernier relevé : {new Date(point.dateDernierReleve).toLocaleString()}</span>}{point.derniersReleves.slice(0,3).map((r,i)=><span key={`${r.date}-${i}`}>Maturité {r.niveauMaturite} · {new Date(r.date).toLocaleDateString()}</span>)}{point.meteo&&<span>Météo · {point.meteo.temperature??'—'}° · humidité {point.meteo.humidite??'—'}% · pluie {point.meteo.precipitation??'—'} mm</span>}</>}
    <small>{point.latitude.toFixed(5)}, {point.longitude.toFixed(5)}</small>
  </div>;
}
