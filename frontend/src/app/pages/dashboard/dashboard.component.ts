import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { SecureGraphDataService } from '../../services/securegraph-data.service';
import { AnalysisSummary, FindingItem } from '../../models/securegraph.models';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="dashboard-header">
      <div>
        <h2>Panel de Consulta y Priorización Explicable</h2>
        <p>Análisis #{{ analysisId }} — Comparación entre SecureGraph Score y Severidad CVSS estándar</p>
      </div>
      <div class="header-actions">
        <a routerLink="/grafo" class="btn btn-outline">Explorar en Grafo</a>
        <button class="btn btn-primary" (click)="exportarReporteJson()">Exportar Informe JSON</button>
      </div>
    </div>

    @if (summary(); as s) {
      <div class="metrics-grid">
        <div class="card metric-card">
          <span>Total Componentes</span>
          <strong>{{ s.components }}</strong>
        </div>
        <div class="card metric-card">
          <span>Hallazgos Detectados</span>
          <strong>{{ s.vulnerabilities }}</strong>
        </div>
        <div class="card metric-card critical">
          <span>Críticas (CRITICAL)</span>
          <strong>{{ s.critical }}</strong>
        </div>
        <div class="card metric-card high">
          <span>Altas (HIGH)</span>
          <strong>{{ s.high }}</strong>
        </div>
        <div class="card metric-card medium">
          <span>Medias (MEDIUM)</span>
          <strong>{{ s.medium }}</strong>
        </div>
      </div>
    }

    <div class="card controls-bar">
      <div class="control-group">
        <label>Criterio de Priorización:</label>
        <button
          class="btn"
          [class.btn-primary]="ordenActual() === 'score'"
          [class.btn-outline]="ordenActual() !== 'score'"
          (click)="cambiarOrden('score')"
        >
          SecureGraph Score (Contextual)
        </button>
        <button
          class="btn"
          [class.btn-primary]="ordenActual() === 'cvss'"
          [class.btn-outline]="ordenActual() !== 'cvss'"
          (click)="cambiarOrden('cvss')"
        >
          Severidad CVSS Pura
        </button>
      </div>

      <div class="control-group">
        <label>Filtrar Severidad:</label>
        <select [ngModel]="filtroSeveridad()" (ngModelChange)="cambiarSeveridad($event)">
          <option value="">Todas las severidades</option>
          <option value="CRITICAL">CRITICAL</option>
          <option value="HIGH">HIGH</option>
          <option value="MEDIUM">MEDIUM</option>
          <option value="LOW">LOW</option>
        </select>
      </div>
    </div>

    <div class="card table-wrapper">
      <table>
        <thead>
          <tr>
            <th>Prioridad</th>
            <th>Componente Maven</th>
            <th>Versión</th>
            <th>Identificador (CVE / OSV)</th>
            <th>Relación / Nivel</th>
            <th>CVSS</th>
            <th>SecureGraph Score</th>
            <th>Explicación de Factores</th>
          </tr>
        </thead>
        <tbody>
          @for (item of findings(); track item.id; let idx = $index) {
            <tr>
              <td><strong>#{{ idx + 1 }}</strong></td>
              <td><code>{{ item.component }}</code></td>
              <td>{{ item.version }}</td>
              <td>
                <div>{{ item.cveId }}</div>
                <small class="muted">{{ item.vulnerabilityId }}</small>
              </td>
              <td>
                {{ item.direct ? 'Directa' : 'Transitiva' }}
                <small class="muted">(Profundidad {{ item.depth }})</small>
              </td>
              <td>
                <span class="badge" [ngClass]="'badge-' + item.severity.toLowerCase()">
                  {{ item.severity }} ({{ item.cvss | number:'1.1-1' }})
                </span>
              </td>
              <td>
                <span class="score-pill">{{ item.secureGraphScore | number:'1.1-1' }} / 100</span>
              </td>
              <td>
                <ul class="reasons-list">
                  @for (reason of item.reasons; track reason) {
                    <li>{{ reason }}</li>
                  }
                </ul>
              </td>
            </tr>
          }
        </tbody>
      </table>
    </div>
  `,
  styles: [`
    .dashboard-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 1.5rem;
    }
    .dashboard-header p {
      color: var(--text-secondary);
      font-size: 0.9rem;
    }
    .header-actions {
      display: flex;
      gap: 0.75rem;
    }
    .metrics-grid {
      display: grid;
      grid-template-columns: repeat(5, 1fr);
      gap: 1rem;
      margin-bottom: 1.25rem;
    }
    .metric-card {
      display: flex;
      flex-direction: column;
      gap: 0.35rem;
    }
    .metric-card span {
      font-size: 0.8rem;
      color: var(--text-secondary);
    }
    .metric-card strong {
      font-size: 1.75rem;
    }
    .metric-card.critical strong { color: #f87171; }
    .metric-card.high strong { color: #fb923c; }
    .metric-card.medium strong { color: #facc15; }
    .controls-bar {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 1.25rem;
    }
    .control-group {
      display: flex;
      align-items: center;
      gap: 0.65rem;
      font-size: 0.85rem;
    }
    select {
      background: #0f172a;
      color: var(--text-primary);
      border: 1px solid var(--border-color);
      border-radius: 6px;
      padding: 0.45rem 0.75rem;
    }
    .table-wrapper {
      overflow-x: auto;
    }
    table {
      width: 100%;
      border-collapse: collapse;
      font-size: 0.875rem;
    }
    th, td {
      text-align: left;
      padding: 0.85rem 0.75rem;
      border-bottom: 1px solid var(--border-color);
      vertical-align: top;
    }
    th {
      color: var(--text-secondary);
      font-weight: 600;
      font-size: 0.78rem;
      text-transform: uppercase;
    }
    .muted {
      display: block;
      color: var(--text-secondary);
      font-size: 0.75rem;
    }
    .score-pill {
      display: inline-block;
      background: rgba(56, 189, 248, 0.16);
      color: var(--accent);
      border: 1px solid var(--accent);
      padding: 0.25rem 0.6rem;
      border-radius: 6px;
      font-weight: 700;
    }
    .reasons-list {
      padding-left: 1.1rem;
      color: #cbd5e1;
      font-size: 0.8rem;
    }
  `]
})
export class DashboardComponent implements OnInit {
  private readonly dataService = inject(SecureGraphDataService);

  readonly analysisId = 42;
  readonly summary = signal<AnalysisSummary | null>(null);
  readonly findings = signal<FindingItem[]>([]);
  readonly ordenActual = signal<'score' | 'cvss'>('score');
  readonly filtroSeveridad = signal<string>('');

  ngOnInit(): void {
    this.cargarDatos();
  }

  cargarDatos(): void {
    this.dataService
      .getHallazgos(this.analysisId, this.ordenActual(), this.filtroSeveridad())
      .subscribe((resp) => {
        this.summary.set(resp.summary);
        this.findings.set(resp.findings);
      });
  }

  cambiarOrden(nuevoOrden: 'score' | 'cvss'): void {
    this.ordenActual.set(nuevoOrden);
    this.cargarDatos();
  }

  cambiarSeveridad(sev: string): void {
    this.filtroSeveridad.set(sev);
    this.cargarDatos();
  }

  exportarReporteJson(): void {
    this.dataService.getReporte(this.analysisId, this.ordenActual()).subscribe((reporte) => {
      const blob = new Blob([JSON.stringify(reporte, null, 2)], { type: 'application/json' });
      const url = URL.createObjectURL(blob);
      const anchor = document.createElement('a');
      anchor.href = url;
      anchor.download = `securegraph-reporte-analisis-${this.analysisId}.json`;
      anchor.click();
      URL.revokeObjectURL(url);
    });
  }
}
