import { Outlet } from 'react-router-dom';
import { Header } from './Header';
import { Sidebar } from './Sidebar';

export function AppShell(){
  return <div className="app-shell">
    <Sidebar/>
    <div className="main-column">
      <Header/>
      <main className="main-content"><Outlet/></main>
      <footer className="page-footer"><LinkBrand/> <span>Une même récolte, quatre regards complémentaires</span></footer>
    </div>
  </div>;
}
function LinkBrand(){return <span className="footer-brand">OLIVECONNECT <i>·</i> AGRICULTURE MÉDITERRANÉENNE</span>}
