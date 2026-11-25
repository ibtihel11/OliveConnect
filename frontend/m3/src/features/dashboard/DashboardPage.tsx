import { Link } from 'react-router-dom';
import { Icon, type IconName } from '../../components/icons/Icon';

const destinations: { title: string; description: string; to: string; icon: IconName; tone: string }[] = [
  { title: 'Équipe', description: 'Ouvriers et savoir-faire', to: '/ressources/ouvriers', icon: 'people', tone: 'sage' },
  { title: 'Parc matériel', description: 'Tracteurs, bennes, matériels', to: '/ressources/tracteurs', icon: 'tractor', tone: 'olive' },
  { title: 'Planification', description: 'Disponibilités et affectations', to: '/ressources/disponibilites', icon: 'calendar', tone: 'lime' },
  { title: 'Logistique', description: 'Stocks et import de données', to: '/ressources/stocks', icon: 'box', tone: 'sand' },
];

export function DashboardPage() {
  return (
    <div className="dashboard-page page-enter">
      <div className="eyebrow"><span className="eyebrow-line" /> L’ESSENTIEL DE VOTRE ACTIVITÉ</div>
      <section className="hero-panel">
        <div className="hero-image" />
        <div className="hero-shade" />
        <div className="hero-content">
          <div className="hero-kicker"><Icon name="leaf" /> LE TEMPS DES OLIVIERS</div>
          <h1>La terre donne.<br /><em>Vous orchestrez.</em></h1>
          <p>Un espace pensé pour prendre soin de celles et ceux qui font vivre vos récoltes.</p>
          <Link className="button button-lime hero-button" to="/ressources/ouvriers">Explorer les ressources <Icon name="arrow" /></Link>
        </div>
        <div className="hero-note"><span>01 — M3</span><span>Ressources & logistique</span></div>
      </section>

      <section className="section-intro">
        <div>
          <div className="eyebrow eyebrow-dark"><span className="eyebrow-line" /> VOTRE ESPACE DE TRAVAIL</div>
          <h2>Tout commence <span>par les bonnes ressources.</span></h2>
        </div>
        <p className="section-copy">Accédez directement aux outils de gestion de votre équipe, de votre parc et de vos opérations.</p>
      </section>

      <section className="destination-grid" aria-label="Accès aux outils M3">
        {destinations.map((destination, index) => (
          <Link to={destination.to} className="destination-card" key={destination.title} style={{ animationDelay: `${index * 70}ms` }}>
            <span className={`destination-icon ${destination.tone}`}><Icon name={destination.icon} /></span>
            <span className="destination-text"><strong>{destination.title}</strong><span>{destination.description}</span></span>
            <Icon name="arrow" className="destination-arrow" />
          </Link>
        ))}
      </section>

      <section className="quiet-note">
        <span className="quiet-note-icon"><Icon name="spark" /></span>
        <p>Un outil au rythme du vivant.<span> Les informations affichées viendront des ressources enregistrées dans OliveConnect.</span></p>
        <span className="quiet-note-label">CULTIVER AVEC INTENTION</span>
      </section>
    </div>
  );
}
