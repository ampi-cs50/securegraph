import { Routes } from '@angular/router';
import { DashboardComponent } from './pages/dashboard/dashboard.component';
import { ProjectsComponent } from './pages/projects/projects.component';
import { UploadSbomComponent } from './pages/upload-sbom/upload-sbom.component';
import { GraphViewComponent } from './pages/graph-view/graph-view.component';

export const routes: Routes = [
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
  { path: 'proyectos', component: ProjectsComponent, title: 'Proyectos — SecureGraph' },
  { path: 'carga-sbom', component: UploadSbomComponent, title: 'Importar SBOM — SecureGraph' },
  { path: 'dashboard', component: DashboardComponent, title: 'Panel de Hallazgos — SecureGraph' },
  { path: 'grafo', component: GraphViewComponent, title: 'Grafo de Dependencias — SecureGraph' },
  { path: '**', redirectTo: 'dashboard' }
];

