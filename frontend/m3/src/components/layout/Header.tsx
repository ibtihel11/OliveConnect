import { Link, useLocation } from 'react-router-dom';
import { useSession } from '../../auth/session';
import { Icon } from '../icons/Icon';

const titles:Record<string,string>={
  '/':'Le domaine, en un regard','/vergers':'Vergers & parcelles','/recoltes':'Demandes de récolte','/tournees':'Tournées de collecte',
  '/alertes':'Alertes agricoles','/notifications':'Notifications','/carte':'Carte des vergers','/indicateurs':'Indicateurs de saison',
  '/pressoir':'Pressoir & réceptions','/connexion':'Votre espace sécurisé',
};
const routeTitles:Record<string,string>={ouvriers:'Ouvriers',tracteurs:'Tracteurs',bennes:'Bennes',materiels:'Matériels',disponibilites:'Disponibilités',affectations:'Affectations',calendrier:'Calendrier',stocks:'Stocks bas',import:'Import des ressources'};

export function Header(){
  const {pathname}=useLocation();
  const {role,token}=useSession();
  const key=pathname.split('/').at(-1)??'';
  const title=titles[pathname]??routeTitles[key]??'OliveConnect';
  const module=pathname.startsWith('/ressources/')||pathname.startsWith('/operations/')?'M3 · RESSOURCES':
    ['/vergers','/recoltes','/tournees'].includes(pathname)?'M1 · COLLECTES':
    ['/alertes','/notifications'].includes(pathname)?'M2 · VIGILANCE':
    ['/carte','/indicateurs','/pressoir'].includes(pathname)?'M4 · OBSERVATOIRE':'ESPACE COMMUN';
  return <header className="pagebar">
    <div className="pagebar-title"><div className="breadcrumb"><span>OLIVECONNECT</span><Icon name="chevron"/><span>{module}</span></div><h1>{title}</h1></div>
    <Link to="/connexion" className="session-indicator"><span className={`session-light${token?' is-online':''}`}/><span>{token?(role??'Session active'):'Session non connectée'}</span><Icon name="arrow"/></Link>
  </header>;
}
