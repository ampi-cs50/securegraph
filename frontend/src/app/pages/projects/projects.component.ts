import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { SecureGraphDataService } from '../../services/securegraph-data.service';
import { Proyecto } from '../../models/securegraph.models';

@Component({
  selector: 'app-projects',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="page-header">
      <div>
        <h2>Proyectos Registrados</h2>
        <p>Administra los proyectos Java/Maven y accede a su historial de análisis SBOM.</p>
      </div>
    </div>

    <div class="grid-layout">
      <section class="card">
        <h3>Nuevo Proyecto</h3>
        <form (ngSubmit)="registrarProyecto()" class="form-stack">
          <label>
            Nombre del Proyecto (Artifact / Repositorio)
            <input
              type="text"
              [(ngModel)]="nombre"
              name="nombre"
              placeholder="Ej: ordenes-service"
              required
            />
          </label>
          <label>
            Descripción / Contexto del Módulo
            <textarea
              [(ngModel)]="descripcion"
              name="descripcion"
              rows="3"
              placeholder="Ej: Microservicio transaccional expuesto en producción"
            ></textarea>
          </label>
          <button type="submit" class="btn btn-primary" [disabled]="!nombre.trim()">
            Registrar Proyecto
          </button>
        </form>
      </section>

      <section class="card">
        <h3>Inventario de Aplicaciones ({{ proyectos().length }})</h3>
        <div class="project-list">
          @for (proj of proyectos(); track proj.id) {
            <div class="project-item">
              <div>
                <h4>{{ proj.nombre }}</h4>
                <p>{{ proj.descripcion || 'Sin descripción adicional' }}</p>
                <small>Análisis realizados: {{ proj.totalAnalisis }} | Creado: {{ proj.createdAt | date:'shortDate' }}</small>
              </div>
              <div class="project-actions">
                <a routerLink="/carga-sbom" class="btn btn-outline">Subir SBOM</a>
                @if (proj.ultimoAnalisisId) {
                  <a routerLink="/dashboard" class="btn btn-primary">Ver Análisis #{{ proj.ultimoAnalisisId }}</a>
                }
              </div>
            </div>
          }
        </div>
      </section>
    </div>
  `,
  styles: [`
    .page-header {
      margin-bottom: 1.5rem;
    }
    .page-header p {
      color: var(--text-secondary);
      font-size: 0.9rem;
    }
    .grid-layout {
      display: grid;
      grid-template-columns: 360px 1fr;
      gap: 1.5rem;
    }
    h3 {
      font-size: 1.05rem;
      margin-bottom: 1rem;
    }
    .form-stack {
      display: flex;
      flex-direction: column;
      gap: 0.9rem;
    }
    label {
      display: flex;
      flex-direction: column;
      gap: 0.35rem;
      font-size: 0.85rem;
      color: var(--text-secondary);
    }
    input, textarea {
      background: #0f172a;
      border: 1px solid var(--border-color);
      border-radius: 6px;
      padding: 0.6rem;
      color: var(--text-primary);
      font-family: inherit;
    }
    .project-list {
      display: flex;
      flex-direction: column;
      gap: 0.85rem;
    }
    .project-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 1rem;
      background: #0f172a;
      border: 1px solid var(--border-color);
      border-radius: 8px;
    }
    .project-item p {
      color: var(--text-secondary);
      font-size: 0.85rem;
      margin: 0.2rem 0;
    }
    .project-item small {
      color: #64748b;
      font-size: 0.75rem;
    }
    .project-actions {
      display: flex;
      gap: 0.5rem;
    }
  `]
})
export class ProjectsComponent implements OnInit {
  private readonly dataService = inject(SecureGraphDataService);

  readonly proyectos = signal<Proyecto[]>([]);
  nombre = '';
  descripcion = '';

  ngOnInit(): void {
    this.cargarProyectos();
  }

  cargarProyectos(): void {
    this.dataService.getProyectos().subscribe((lista) => this.proyectos.set(lista));
  }

  registrarProyecto(): void {
    if (!this.nombre.trim()) return;
    this.dataService
      .crearProyecto({ nombre: this.nombre.trim(), descripcion: this.descripcion.trim() })
      .subscribe(() => {
        this.nombre = '';
        this.descripcion = '';
        this.cargarProyectos();
      });
  }
}
