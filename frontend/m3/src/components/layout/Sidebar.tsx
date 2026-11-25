import { useState } from 'react';
import { Link, NavLink, useLocation } from 'react-router-dom';
import { useSession } from '../../auth/session';
import type { Role } from '../../api/types';
import { Icon, type IconName } from '../icons/Icon';

type Entry = { label:string; to:string; icon:IconName; roles?:Role[] };
type Group = { key:string; code:string; name:string; items:Entry[] };
const groups:Group[]=[
  {key:'m1',code:'M1',name:'Collectes',items:[
    {label:'Vergers & parcelles',to:'/vergers',icon:'leaf',roles:['AGRICULTEUR','RESPONSABLE_COLLECTE','ADMINISTRATEUR']},
    {label:'Demandes de récolte',to:'/recoltes',icon:'calendar',roles:['AGRICULTEUR','RESPONSABLE_COLLECTE','CHEF_EQUIPE','ADMINISTRATEUR']},
    {label:'Tournées',to:'/tournees',icon:'arrows',roles:['RESPONSABLE_COLLECTE','CHEF_EQUIPE','ADMINISTRATEUR']},
  ]},
  {key:'m2',code:'M2',name:'Alertes',items:[
    {label:'Alertes agricoles',to:'/alertes',icon:'spark',roles:['AGRICULTEUR','RESPONSABLE_COLLECTE','CHEF_EQUIPE','ADMINISTRATEUR']},
    {label:'Notifications',to:'/notifications',icon:'box'},
  ]},
  {key:'m3',code:'M3',name:'Ressources',items:[
    {label:'Ouvriers',to:'/ressources/ouvriers',icon:'people',roles:['RESPONSABLE_LOGISTIQUE']},
    {label:'Tracteurs',to:'/ressources/tracteurs',icon:'tractor',roles:['RESPONSABLE_LOGISTIQUE']},
    {label:'Bennes',to:'/ressources/bennes',icon:'truck',roles:['RESPONSABLE_LOGISTIQUE']},
    {label:'Matériels',to:'/ressources/materiels',icon:'tools',roles:['RESPONSABLE_LOGISTIQUE']},
    {label:'Disponibilités',to:'/operations/disponibilites',icon:'arrows',roles:['RESPONSABLE_LOGISTIQUE','RESPONSABLE_COLLECTE']},
    {label:'Affectations',to:'/operations/affectations',icon:'calendar',roles:['RESPONSABLE_LOGISTIQUE','RESPONSABLE_COLLECTE']},
    {label:'Calendrier',to:'/operations/calendrier',icon:'calendar'},
    {label:'Stocks bas',to:'/operations/stocks',icon:'box',roles:['RESPONSABLE_LOGISTIQUE']},
    {label:'Importer',to:'/operations/import',icon:'upload',roles:['RESPONSABLE_LOGISTIQUE']},
  ]},
  {key:'m4',code:'M4',name:'Cartographie',items:[
    {label:'Carte des vergers',to:'/carte',icon:'leaf'},
    {label:'Indicateurs',to:'/indicateurs',icon:'grid',roles:['RESPONSABLE_COLLECTE','ADMINISTRATEUR']},
    {label:'Pressoir & réceptions',to:'/pressoir',icon:'truck',roles:['RESPONSABLE_PRESSOIR']},
  ]},
];

function ModuleLinks({group,role,onNavigate}:{group:Group;role:Role|null;onNavigate:()=>void}){
  const visible=group.items.filter(item=>!item.roles||!role||item.roles.includes(role));
  if(!visible.length)return null;
  return <div className="module-links"><div className="module-links-heading">{group.code} · {group.name}</div>{visible.map(item=><NavLink key={item.to} to={item.to} onClick={onNavigate} className={({isActive})=>`module-link${isActive?' is-active':''}`}><Icon name={item.icon}/><span>{item.label}</span></NavLink>)}</div>;
}

export function Sidebar(){
  const {role}=useSession();
  const {pathname}=useLocation();
  const [mobileOpen,setMobileOpen]=useState(false);
  const close=()=>{
    setMobileOpen(false);
    document.querySelectorAll<HTMLDetailsElement>('.module-menu[open]').forEach(menu=>menu.removeAttribute('open'));
  };
  const activeGroup=groups.find(group=>group.items.some(item=>item.to===pathname||pathname.startsWith(`${item.to}/`)))?.key;
  return <header className={`site-nav${mobileOpen?' mobile-open':''}`}>
    <div className="nav-main">
      <Link to="/" className="brand" onClick={close} aria-label="OliveConnect, accueil"><span className="brand-mark"><Icon name="leaf"/></span><span className="brand-word">olive<span>connect</span></span><span className="brand-period">EST. OLIVE SEASON</span></Link>
      <button className="nav-toggle" type="button" aria-expanded={mobileOpen} aria-label={mobileOpen?'Fermer la navigation':'Ouvrir la navigation'} onClick={()=>setMobileOpen(value=>!value)}><span/><span/></button>
      <nav className="primary-nav" aria-label="Modules OliveConnect">
        <NavLink to="/" end className={({isActive})=>`home-nav${isActive?' is-active':''}`}><Icon name="grid"/><span>Accueil</span></NavLink>
        {groups.map(group=><details key={group.key} className={`module-menu${activeGroup===group.key?' is-active':''}`}>
          <summary><span className="module-code">{group.code}</span><span>{group.name}</span><Icon name="chevron" className="module-chevron"/></summary>
          <div className="module-menu-panel"><ModuleLinks group={group} role={role} onNavigate={close}/></div>
        </details>)}
      </nav>
      <Link to="/connexion" className="account-link" aria-label="Gestion de session"><span className="account-status"/><span>{role??'Connexion'}</span><span className="account-avatar">{role?role.slice(0,2):'OC'}</span></Link>
    </div>
    {mobileOpen&&<>
      <button className="mobile-nav-scrim" aria-label="Fermer la navigation" onClick={close}/>
      <nav className="mobile-navigation" aria-label="Navigation mobile">
        <NavLink to="/" end onClick={close} className={({isActive})=>`mobile-home${isActive?' is-active':''}`}><Icon name="grid"/>Vue d’ensemble</NavLink>
        {groups.map(group=><ModuleLinks key={group.key} group={group} role={role} onNavigate={close}/>)}
        <Link to="/connexion" className="mobile-account" onClick={close}>Gérer la session <span>{role??'Jeton JWT'}</span></Link>
      </nav>
    </>}
  </header>;
}
