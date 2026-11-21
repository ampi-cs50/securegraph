import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { SecureGraphDataService } from '../../services/securegraph-data.service';
import { Proyecto } from '../../models/securegraph.models';

interface SbomPreviewMetadata {
  bomFormat: string;
  specVersion: string;
  serialNumber?: string;
  rootComponent?: string;
  componentsCount: number;
  dependenciesCount: number;
}

@Component({
  selector: 'app-upload-sbom',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="upload-container card">
      <h2>Importar Inventario SBOM (CycloneDX JSON)</h2>
      <p class="subtitle">
        Selecciona el proyecto destino y adjunta un archivo generado desde Apache Maven
        (<code>cyclonedx-maven-plugin</code>) para extraer las dependencias directas y transitivas.
      </p>

      @if (proyectos().length === 0) {
        <div class="alert alert-warning">
          <span>No se encontraron proyectos registrados en el sistema.</span>
          <a routerLink="/proyectos" class="btn btn-outline btn-sm">Registrar un Proyecto</a>
        </div>
      } @else {
        <div class="project-selector">
          <label for="projectSelect">Proyecto Asociado</label>
          <select id="projectSelect" [ngModel]="selectedProjectId()" (ngModelChange)="selectedProjectId.set(+$event)">
            @for (proj of proyectos(); track proj.id) {
              <option [value]="proj.id">{{ proj.nombre }}</option>
            }
          </select>
        </div>
      }

      <div
        class="dropzone"
        [class.dropzone-active]="isDragging()"
        [class.dropzone-valid]="sbomPreview() !== null"
        (click)="fileInput.click()"
        (dragover)="onDragOver($event)"
        (dragleave)="onDragLeave($event)"
        (drop)="onFileDrop($event)"
      >
        <input
          #fileInput
          type="file"
          accept=".json,application/json"
          (change)="onFileSelected($event)"
          hidden
        />
        <div class="dropzone-content">
          <span class="file-badge" [class.badge-valid]="sbomPreview() !== null">
            {{ sbomPreview() ? 'CycloneDX Verificado' : 'JSON CycloneDX' }}
          </span>

          @if (selectedFile(); as file) {
            <strong class="file-name">{{ file.name }}</strong>
            <small class="file-size">{{ (file.size / 1024) | number:'1.1-1' }} KB seleccionados</small>
            <span class="change-hint">Haz clic o arrastra para cambiar de archivo</span>
          } @else {
            <strong>Seleccionar o arrastrar archivo SBOM CycloneDX (.json)</strong>
            <small>Archivo de prueba disponible en <code>samples/sample-maven-cyclonedx.json</code></small>
          }
        </div>
      </div>

      @if (sbomPreview(); as preview) {
        <div class="preview-box">
          <div class="preview-header">
            <h4>Estructura CycloneDX Validada</h4>
            <span class="spec-pill">Versión {{ preview.specVersion }}</span>
          </div>
          <div class="preview-grid">
            <div class="preview-stat">
              <span class="stat-label">Componente Raíz:</span>
              <strong class="stat-value">{{ preview.rootComponent || 'No especificado' }}</strong>
            </div>
            <div class="preview-stat">
              <span class="stat-label">Componentes (Librerías):</span>
              <strong class="stat-value">{{ preview.componentsCount }}</strong>
            </div>
            <div class="preview-stat">
              <span class="stat-label">Relaciones de Dependencia:</span>
              <strong class="stat-value">{{ preview.dependenciesCount }}</strong>
            </div>
          </div>
        </div>
      }

      @if (errorMessage()) {
        <div class="alert alert-error">{{ errorMessage() }}</div>
      }

      <div class="actions">
        <button
          class="btn btn-primary"
          [disabled]="!selectedFile() || !sbomPreview() || uploading() || proyectos().length === 0"
          (click)="procesarArchivo()"
        >
          {{ uploading() ? 'Procesando árbol y consultando OSV...' : 'Ejecutar Análisis de Vulnerabilidades' }}
        </button>
      </div>
    </div>
  `,
  styles: [`
    .upload-container {
      max-width: 42.5rem;
      margin: 0 auto;
    }
    .subtitle {
      color: var(--text-secondary);
      font-size: 0.9rem;
      margin: 0.5rem 0 1.25rem;
    }
    .project-selector {
      display: flex;
      flex-direction: column;
      gap: 0.4rem;
      margin-bottom: 1.25rem;
    }
    .project-selector label {
      font-size: 0.85rem;
      color: var(--text-secondary);
    }
    .project-selector select {
      background: #0f172a;
      border: 0.0625rem solid var(--border-color);
      border-radius: 0.375rem;
      padding: 0.6rem;
      color: var(--text-primary);
      font-family: inherit;
      font-size: 0.9rem;
    }
    .dropzone {
      border: 0.125rem dashed var(--border-color);
      border-radius: 0.625rem;
      padding: 2.25rem 1.5rem;
      text-align: center;
      cursor: pointer;
      background: #0f172a;
      transition: all 0.2s ease;
    }
    .dropzone:hover,
    .dropzone.dropzone-active {
      border-color: var(--accent);
      background: rgba(56, 189, 248, 0.05);
    }
    .dropzone.dropzone-valid {
      border-color: #10b981;
      background: rgba(16, 185, 129, 0.04);
    }
    .dropzone-content {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 0.5rem;
    }
    .file-badge {
      display: inline-block;
      padding: 0.3rem 0.65rem;
      border-radius: 0.375rem;
      font-size: 0.75rem;
      font-weight: 700;
      letter-spacing: 0.05rem;
      background: rgba(56, 189, 248, 0.15);
      color: var(--accent);
      border: 0.0625rem solid var(--accent);
    }
    .file-badge.badge-valid {
      background: rgba(16, 185, 129, 0.15);
      color: #34d399;
      border-color: #10b981;
    }
    .file-name {
      color: #f8fafc;
      font-size: 1rem;
    }
    .file-size {
      color: var(--text-secondary);
      font-size: 0.8rem;
    }
    .change-hint {
      color: #94a3b8;
      font-size: 0.75rem;
      margin-top: 0.25rem;
    }
    .preview-box {
      margin-top: 1.25rem;
      padding: 1rem;
      background: #0f172a;
      border: 0.0625rem solid #10b981;
      border-radius: 0.5rem;
    }
    .preview-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 0.75rem;
      padding-bottom: 0.5rem;
      border-bottom: 0.0625rem solid rgba(255, 255, 255, 0.08);
    }
    .preview-header h4 {
      font-size: 0.9rem;
      color: #34d399;
      margin: 0;
    }
    .spec-pill {
      font-size: 0.75rem;
      background: rgba(16, 185, 129, 0.2);
      color: #34d399;
      padding: 0.2rem 0.5rem;
      border-radius: 0.25rem;
      font-weight: 600;
    }
    .preview-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(10rem, 1fr));
      gap: 0.75rem;
    }
    .preview-stat {
      display: flex;
      flex-direction: column;
      gap: 0.2rem;
    }
    .stat-label {
      font-size: 0.75rem;
      color: var(--text-secondary);
    }
    .stat-value {
      font-size: 0.875rem;
      color: #f8fafc;
    }
    .alert {
      margin-top: 1rem;
      padding: 0.75rem 1rem;
      border-radius: 0.375rem;
      font-size: 0.85rem;
      display: flex;
      justify-content: space-between;
      align-items: center;
      gap: 0.75rem;
    }
    .alert-warning {
      background: rgba(234, 179, 8, 0.12);
      border: 0.0625rem solid rgba(234, 179, 8, 0.4);
      color: #fde047;
    }
    .alert-error {
      background: rgba(239, 68, 68, 0.12);
      border: 0.0625rem solid var(--critical);
      color: #fca5a5;
    }
    .btn-sm {
      padding: 0.35rem 0.75rem;
      font-size: 0.75rem;
    }
    .actions {
      margin-top: 1.5rem;
      display: flex;
      justify-content: flex-end;
    }
    @media (max-width: 40rem) {
      .preview-grid {
        grid-template-columns: 1fr;
      }
      .actions button {
        width: 100%;
      }
    }
  `]
})
export class UploadSbomComponent implements OnInit {
  private readonly dataService = inject(SecureGraphDataService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  readonly proyectos = signal<Proyecto[]>([]);
  readonly selectedProjectId = signal<number>(1);
  readonly selectedFile = signal<File | null>(null);
  readonly sbomPreview = signal<SbomPreviewMetadata | null>(null);
  readonly uploading = signal<boolean>(false);
  readonly errorMessage = signal<string>('');
  readonly isDragging = signal<boolean>(false);

  ngOnInit(): void {
    const queryProyectoId = Number(this.route.snapshot.queryParamMap.get('proyectoId'));
    this.dataService.getProyectos().subscribe((lista) => {
      this.proyectos.set(lista);
      if (queryProyectoId && lista.some((p) => p.id === queryProyectoId)) {
        this.selectedProjectId.set(queryProyectoId);
      } else if (lista.length > 0) {
        this.selectedProjectId.set(lista[0].id);
      }
    });
  }

  onDragOver(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging.set(true);
  }

  onDragLeave(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging.set(false);
  }

  onFileDrop(event: DragEvent): void {
    event.preventDefault();
    event.stopPropagation();
    this.isDragging.set(false);

    const files = event.dataTransfer?.files;
    if (files && files.length > 0) {
      this.validarYProcesarSeleccion(files[0]);
    }
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0] ?? null;
    if (file) {
      this.validarYProcesarSeleccion(file);
    }
  }

  private validarYProcesarSeleccion(file: File): void {
    this.errorMessage.set('');
    this.selectedFile.set(null);
    this.sbomPreview.set(null);

      if (!file.name.toLowerCase().endsWith('.json')) {
        this.errorMessage.set(
          'Extensión inválida: El archivo debe tener extensión .json y responder al formato CycloneDX.'
        );
        return;
      }

      const reader = new FileReader();
      reader.onload = () => {
        try {
          const content = JSON.parse(reader.result as string);

          if (!content || typeof content !== 'object') {
            this.errorMessage.set('El archivo no contiene un objeto JSON válido.');
            return;
          }

          const bomFormat = String(content.bomFormat || '').trim().toLowerCase();
          if (bomFormat !== 'cyclonedx') {
            this.errorMessage.set(
              'Estructura incompatible: El archivo no es un SBOM CycloneDX. Debe contener la propiedad "bomFormat": "CycloneDX".'
            );
            return;
          }

          const specVersion = String(content.specVersion || '1.4');
          const components = Array.isArray(content.components) ? content.components : [];
          const dependencies = Array.isArray(content.dependencies) ? content.dependencies : [];
          const rootComponent = content.metadata?.component?.name || content.metadata?.component?.['bom-ref'] || undefined;

          this.selectedFile.set(file);
          this.sbomPreview.set({
            bomFormat: 'CycloneDX',
            specVersion,
            serialNumber: content.serialNumber,
            rootComponent,
            componentsCount: components.length,
            dependenciesCount: dependencies.length
          });
        } catch {
          this.errorMessage.set(
            'Error de sintaxis: No se pudo parsear el archivo como JSON válido. Verifica la integridad del archivo.'
          );
        }
      };

      reader.onerror = () => {
        this.errorMessage.set('Error de lectura: No se pudo leer el archivo seleccionado.');
      };

      reader.readAsText(file);
    }

    procesarArchivo(): void {
      const file = this.selectedFile();
      const projectId = this.selectedProjectId();
      if(!file || !projectId || this.uploading()) return;

    this.uploading.set(true);
        this.errorMessage.set('');

        this.dataService.subirSbom(projectId, file).subscribe({
          next: (response) => {
            this.uploading.set(false);
            this.router.navigate(['/dashboard'], {
              queryParams: { analysisId: response.analysisId }
            });
          },
          error: () => {
            this.uploading.set(false);
            this.errorMessage.set(
              'Error de comunicación con el servidor al procesar el archivo SBOM. Verifica que el backend esté activo o activa el Modo Mock.'
            );
          }
        });
      }
    }
