import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { SecureGraphDataService } from './services/securegraph-data.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <div class="layout-shell">
      <header class="topbar">
        <div class="brand">
          <div class="brand-badge">SG</div>
          <div>
            <h1>SecureGraph</h1>
            <small>Priorización Explicable de Vulnerabilidades Maven mediante Grafos</small>
          </div>
        </div>

        <div class="topbar-actions">
          <span class="mode-indicator" [class.live]="!dataService.useMockMode()">
            {{ dataService.useMockMode() ? 'Modo Mock (Contrato v1.0)' : 'API Backend (localhost:8080)' }}
          </span>
          <button class="btn btn-outline" (click)="dataService.toggleMode()">
            Cambiar a {{ dataService.useMockMode() ? 'API Real' : 'Modo Mock' }}
          </button>
        </div>
      </header>

      <div class="main-container">
        <aside class="sidebar">
          <nav class="nav-menu">
            <a routerLink="/dashboard" routerLinkActive="active" class="nav-item">
              Panel de Hallazgos
            </a>
            <a routerLink="/grafo" routerLinkActive="active" class="nav-item">
              Grafo de Dependencias
            </a>
            <a routerLink="/proyectos" routerLinkActive="active" class="nav-item">
              Gestión de Proyectos
            </a>
            <a routerLink="/carga-sbom" routerLinkActive="active" class="nav-item">
              Importar SBOM CycloneDX
            </a>
          </nav>

          <div class="system-footer">
            <p><strong>SecureGraph v0.1.0</strong></p>
            <p>TFI — UTN FRLP</p>
            <p>Análisis SCA &amp; Grafos</p>
          </div>
        </aside>

        <main class="workspace">
          <router-outlet></router-outlet>
        </main>
      </div>
    </div>
  `,
  styles: [`
    .layout-shell {
      min-height: 100vh;
      display: flex;
      flex-direction: column;
    }
    .topbar {
      height: 68px;
      background: #0b1120;
      border-bottom: 1px solid var(--border-color);
      padding: 0 1.5rem;
      display: flex;
      align-items: center;
      justify-content: space-between;
    }
    .brand {
      display: flex;
      align-items: center;
      gap: 0.75rem;
    }
    .brand-badge {
      width: 36px;
      height: 36px;
      border-radius: 8px;
      background: rgba(56, 189, 248, 0.15);
      border: 1px solid var(--accent);
      color: var(--accent);
      display: flex;
      align-items: center;
      justify-content: center;
      font-weight: 700;
      font-size: 0.9rem;
      letter-spacing: 0.03em;
    }
    .brand h1 {
      font-size: 1.2rem;
      font-weight: 700;
      letter-spacing: -0.02em;
    }
    .brand small {
      color: var(--text-secondary);
      font-size: 0.75rem;
      display: block;
    }
    .topbar-actions {
      display: flex;
      align-items: center;
      gap: 0.85rem;
    }
    .mode-indicator {
      font-size: 0.75rem;
      padding: 0.3rem 0.7rem;
      border-radius: 999px;
      background: rgba(234, 179, 8, 0.15);
      color: #fde047;
      border: 1px solid #eab308;
      font-weight: 600;
    }
    .mode-indicator.live {
      background: rgba(34, 197, 94, 0.15);
      color: #86efac;
      border-color: #22c55e;
    }
    .main-container {
      flex: 1;
      display: flex;
    }
    .sidebar {
      width: 250px;
      background: #111827;
      border-right: 1px solid var(--border-color);
      padding: 1.25rem 0.85rem;
      display: flex;
      flex-direction: column;
      justify-content: space-between;
    }
    .nav-menu {
      display: flex;
      flex-direction: column;
      gap: 0.4rem;
    }
    .nav-item {
      text-decoration: none;
      color: var(--text-secondary);
      padding: 0.7rem 0.9rem;
      border-radius: 8px;
      font-size: 0.9rem;
      font-weight: 500;
      transition: all 0.15s ease;
    }
    .nav-item:hover {
      background: rgba(56, 189, 248, 0.08);
      color: var(--text-primary);
    }
    .nav-item.active {
      background: rgba(56, 189, 248, 0.16);
      color: var(--accent);
      font-weight: 600;
    }
    .system-footer {
      font-size: 0.75rem;
      color: var(--text-secondary);
      padding: 0.75rem;
      border-top: 1px solid var(--border-color);
      line-height: 1.4;
    }
    .workspace {
      flex: 1;
      padding: 1.75rem;
      overflow-y: auto;
    }
  `]
})
export class AppComponent {
  readonly dataService = inject(SecureGraphDataService);
}
