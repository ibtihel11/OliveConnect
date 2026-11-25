import { Link, useParams } from 'react-router-dom';
import { Icon } from '../../components/icons/Icon';

const sections: Record<string, { title: string; intro: string; icon: 'people' | 'tractor' | 'truck' | 'tools' | 'calendar' | 'arrows' | 'box' | 'upload'; endpoints: string[] }> = {
  ouvriers: { title: 'Ouvriers', intro: 'La gestion de votre équipe sera disponible ici.', icon: 'people', endpoints: ['/api/ressources/ouvriers'] },
  tracteurs: { title: 'Tracteurs', intro: 'Le suivi de votre parc de tracteurs sera disponible ici.', icon: 'tractor', endpoints: ['/api/ressources/tracteurs'] },
  bennes: { title: 'Bennes', intro: 'La gestion des bennes sera disponible ici.', icon: 'truck', endpoints: ['/api/ressources/bennes'] },
  materiels: { title: 'Matériels', intro: 'Le suivi de vos matériels et de leurs stocks sera disponible ici.', icon: 'tools', endpoints: ['/api/ressources/materiels'] },
  disponibilites: { title: 'Disponibilités', intro: 'La recherche par période utilisera les disponibilités enregistrées et les affectations actives.', icon: 'arrows', endpoints: ['/api/ressources/{type}/disponibles'] },
  affectations: { title: 'Affectations', intro: 'La consultation et la création utiliseront les affectations réelles de M3.', icon: 'calendar', endpoints: ['/api/affectations'] },
  calendrier: { title: 'Calendrier', intro: 'Le calendrier affichera les affectations réelles enregistrées dans M3.', icon: 'calendar', endpoints: ['/api/calendrier'] },
  stocks: { title: 'Stocks bas', intro: 'Les matériels sous leur seuil minimum seront consultés depuis le backend.', icon: 'box', endpoints: ['/api/ressources/materiels/stock-bas'] },
  import: { title: 'Import de ressources', intro: 'Les fichiers JSON et CSV pourront être importés avec leur rapport de validation.', icon: 'upload', endpoints: ['/api/ressources/import'] },
};

export function SectionPage() {
  const { section } = useParams();
  const item = section ? sections[section] : undefined;
  if (!item) return <div className="empty-state"><Icon name="leaf" /><p>Cette rubrique M3 n’existe pas.</p><Link to="/">Retour à la vue d’ensemble</Link></div>;

  return (
    <section className="section-page page-enter">
      <div className="eyebrow eyebrow-dark"><span className="eyebrow-line" /> ESPACE M3 · RESSOURCES</div>
      <div className="section-heading">
        <span className="large-section-icon"><Icon name={item.icon} /></span>
        <div><h1>{item.title}</h1><p>{item.intro}</p></div>
      </div>
      <div className="preparation-card">
        <span className="preparation-mark"><Icon name="spark" /></span>
        <div>
          <span className="preparation-label">FONDATION PRÊTE</span>
          <h2>Une interface reliée aux contrats existants.</h2>
          <p>Cette première phase met en place l’identité visuelle, la navigation M3 et le client API. Les données métier ne sont pas simulées et cette page sera raccordée lors de la phase fonctionnelle.</p>
        </div>
      </div>
      <div className="contract-strip">
        <span>CONTRAT BACKEND REPÉRÉ</span>
        <div>{item.endpoints.map((endpoint) => <code key={endpoint}>{endpoint}</code>)}</div>
      </div>
      <Link className="back-link" to="/"><Icon name="arrow" /> Retour à la vue d’ensemble</Link>
    </section>
  );
}
