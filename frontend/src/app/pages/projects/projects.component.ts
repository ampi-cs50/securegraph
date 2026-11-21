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
            <span class="label-row">
              <span>Nombre del Proyecto (Artifact / Repositorio)</span>
              <small class="char-counter">{{ nombre.length }} / 100</small>
            </span>
            <input
              type="text"
              [(ngModel)]="nombre"
              name="nombre"
              maxlength="100"
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

          @if (successMessage()) {
            <div class="alert alert-success">{{ successMessage() }}</div>
          }

          @if (errorMessage()) {
            <div class="alert alert-error">{{ errorMessage() }}</div>
          }

          <button type="submit" class="btn btn-primary" [disabled]="!nombre.trim() || submitting()">
            {{ submitting() ? 'Registrando...' : 'Registrar Proyecto' }}
          </button>
        </form>
      </section>

      <section class="card">
        <h3>Inventario de Aplicaciones ({{ proyectos().length }})</h3>

        @if (loading()) {
          <div class="empty-state">Cargando proyectos registrados...</div>
        } @else {
          <div class="project-list">
            @for (proj of proyectos(); track proj.id) {
              <div class="project-item">
                <div class="project-info">
                  <h4>{{ proj.nombre }}</h4>
                  <p>{{ proj.descripcion || 'Sin descripción adicional' }}</p>
                  <small>Análisis realizados: {{ proj.totalAnalisis }} | Creado: {{ proj.createdAt | date:'shortDate' }}</small>
                </div>
                <div class="project-actions">
                  <a
                    routerLink="/carga-sbom"
                    [queryParams]="{ proyectoId: proj.id }"
                    class="btn btn-outline"
                  >
                    Subir SBOM
                  </a>
                  @if (proj.ultimoAnalisisId) {
                    <a
                      routerLink="/dashboard"
                      [queryParams]="{ analysisId: proj.ultimoAnalisisId }"
                      class="btn btn-primary"
                    >
                      Ver Análisis #{{ proj.ultimoAnalisisId }}
                    </a>
                  }
                </div>
              </div>
            } @empty {
              <div class="empty-state">
                No hay proyectos registrados todavía. Crea el primero desde el formulario para comenzar un análisis SBOM.
              </div>
            }
          </div>
        }
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
      grid-template-columns: minmax(18rem, 22.5rem) 1fr;
      gap: 1.5rem;
      align-items: start;
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
    .label-row {
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: 0.5rem;
    }
    .char-counter {
      font-size: 0.72rem;
      color: #64748b;
    }
    input, textarea {
      background: #0f172a;
      border: 0.0625rem solid var(--border-color);
      border-radius: 0.375rem;
      padding: 0.6rem;
      color: var(--text-primary);
      font-family: inherit;
    }
    .alert {
      padding: 0.65rem 0.75rem;
      border-radius: 0.375rem;
      font-size: 0.82rem;
    }
    .alert-success {
      background: rgba(34, 197, 94, 0.15);
      border: 0.0625rem solid var(--low);
      color: #86efac;
    }
    .alert-error {
      background: rgba(239, 68, 68, 0.15);
      border: 0.0625rem solid var(--critical);
      color: #fca5a5;
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
      gap: 1rem;
      padding: 1rem;
      background: #0f172a;
      border: 0.0625rem solid var(--border-color);
      border-radius: 0.5rem;
    }
    .project-info {
      min-width: 0;
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
      flex-wrap: wrap;
      gap: 0.5rem;
      flex-shrink: 0;
    }
    .empty-state {
      padding: 1.5rem;
      text-align: center;
      color: var(--text-secondary);
      font-size: 0.88rem;
      background: #0f172a;
      border: 0.0625rem dashed var(--border-color);
      border-radius: 0.5rem;
    }
    @media (max-width: 60rem) {
      .grid-layout {
        grid-template-columns: 1fr;
      }
      .project-item {
        flex-direction: column;
        align-items: flex-start;
      }
    }
  `]
})
export class ProjectsComponent implements OnInit {
  private readonly dataService = inject(SecureGraphDataService);

  readonly proyectos = signal<Proyecto[]>([]);
  readonly loading = signal<boolean>(false);
  readonly submitting = signal<boolean>(false);
  readonly successMessage = signal<string>('');
  readonly errorMessage = signal<string>('');

  nombre = '';
  descripcion = '';

  ngOnInit(): void {
    this.cargarProyectos();
  }

  cargarProyectos(): void {
    this.loading.set(true);
    this.errorMessage.set('');
    this.dataService.getProyectos().subscribe({
      next: (lista) => {
        this.proyectos.set(lista);
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.errorMessage.set(
          'No se pudo obtener el listado de proyectos. Verifica que el backend esté activo o cambia a Modo Mock.'
        );
      }
    });
  }

  registrarProyecto(): void {
    const nombreLimpio = this.nombre.trim();
    if (!nombreLimpio || this.submitting()) return;

    this.submitting.set(true);
    this.successMessage.set('');
    this.errorMessage.set('');

    this.dataService
      .crearProyecto({ nombre: nombreLimpio, descripcion: this.descripcion.trim() })
      .subscribe({
        next: (creado) => {
          this.submitting.set(false);
          this.nombre = '';
          this.descripcion = '';
          this.successMessage.set(`Proyecto "${creado.nombre}" registrado correctamente.`);
          this.cargarProyectos();
        },
        error: () => {
          this.submitting.set(false);
          this.errorMessage.set(
            'Error al registrar el proyecto. Verifica los datos ingresados o la conexión con el servidor.'
          );
        }
      });
  }
}
