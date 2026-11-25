import { Navigate, Route, Routes } from 'react-router-dom';
import { AppShell } from '../components/layout/AppShell';
import { DashboardPage } from '../features/m4/DashboardPage';
import { ResourcesPage } from '../features/m3/ResourcesPage';
import { OperationsPage } from '../features/m3/OperationsPage';
import { MapPage } from '../features/m4/MapPage';
import { PressPage } from '../features/m4/PressPage';
import { DocumentedModulePage } from '../features/m1m2/DocumentedModules';
import { SessionPage } from '../features/auth/SessionPage';

export default function App(){return <Routes><Route element={<AppShell/>}><Route index element={<DashboardPage/>}/><Route path="connexion" element={<SessionPage/>}/><Route path="carte" element={<MapPage/>}/><Route path="indicateurs" element={<DashboardPage/>}/><Route path="pressoir" element={<PressPage/>}/><Route path="ressources/:section" element={<ResourcesPage/>}/><Route path="operations/:section" element={<OperationsPage/>}/><Route path="vergers" element={<DocumentedModulePage/>}/><Route path="recoltes" element={<DocumentedModulePage/>}/><Route path="tournees" element={<DocumentedModulePage/>}/><Route path="alertes" element={<DocumentedModulePage/>}/><Route path="notifications" element={<DocumentedModulePage/>}/><Route path="*" element={<Navigate to="/" replace/>}/></Route></Routes>}
