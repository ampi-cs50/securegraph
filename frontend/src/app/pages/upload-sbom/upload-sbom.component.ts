import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { SecureGraphDataService } from '../../services/securegraph-data.service';
import { Proyecto } from '../../models/securegraph.models';

@Component({
  selector: 'app-upload-sbom',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
    <div class="upload-container card">
      <h2>Importar Inventario SBOM (CycloneDX JSON)</h2>
      <p class="subtitle">
        Selecciona el proyecto destino y adjunta un archivo generado desde Apache Maven
        (<code>cyclonedx-maven-plugin</code>) para extraer las dependencias directas y transitivas.
      </p>

      <div class="project-selector">
        <label for="projectSelect">Proyecto Asociado</label>
        <select id="projectSelect" [ngModel]="selectedProjectId()" (ngModelChange)="selectedProjectId.set(+$event)">
          @for (proj of proyectos(); track proj.id) {
            <option [value]="proj.id">{{ proj.nombre }}</option>
          }
        </select>
      </div>

      <div class="dropzone" (click)="fileInput.click()">
        <input
          #fileInput
          type="file"
          accept=".json,application/json"
          (change)="onFileSelected($event)"
          hidden
        />
        <div class="dropzone-content">
          <span class="file-badge">JSON</span>
          @if (selectedFile()) {
            <strong>{{ selectedFile()!.name }}</strong>
            <small>{{ (selectedFile()!.size / 1024) | number:'1.1-1' }} KB seleccionados</small>
          } @else {
            <strong>Seleccionar archivo SBOM CycloneDX (.json)</strong>
            <small>Archivo de prueba disponible en samples/sample-maven-cyclonedx.json</small>
          }
        </div>
      </div>

      @if (errorMessage()) {
        <div class="error-box">{{ errorMessage() }}</div>
      }

      <div class="actions">
        <button
          class="btn btn-primary"
          [disabled]="!selectedFile() || uploading()"
          (click)="procesarArchivo()"
        >
          {{ uploading() ? 'Procesando árbol y consultando OSV...' : 'Ejecutar Análisis de Vulnerabilidades' }}
        </button>
      </div>
    </div>
  `,
  styles: [`
    .upload-container {
      max-width: 680px;
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
      border: 1px solid var(--border-color);
      border-radius: 6px;
      padding: 0.6rem;
      color: var(--text-primary);
      font-family: inherit;
    }
    .dropzone {
      border: 2px dashed var(--border-color);
      border-radius: 10px;
      padding: 2.5rem 1.5rem;
      text-align: center;
      cursor: pointer;
      background: #0f172a;
      transition: border-color 0.2s ease;
    }
    .dropzone:hover {
      border-color: var(--accent);
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
      border-radius: 6px;
      font-size: 0.75rem;
      font-weight: 700;
      letter-spacing: 0.05em;
      background: rgba(56, 189, 248, 0.15);
      color: var(--accent);
      border: 1px solid var(--accent);
    }
    .dropzone-content small {
      color: var(--text-secondary);
    }
    .error-box {
      margin-top: 1rem;
      padding: 0.75rem;
      border-radius: 6px;
      background: rgba(239, 68, 68, 0.15);
      border: 1px solid var(--critical);
      color: #fca5a5;
      font-size: 0.85rem;
    }
    .actions {
      margin-top: 1.5rem;
      display: flex;
      justify-content: flex-end;
    }
  `]
})
export class UploadSbomComponent implements OnInit {
  private readonly dataService = inject(SecureGraphDataService);
  private readonly router = inject(Router);

  readonly proyectos = signal<Proyecto[]>([]);
  readonly selectedProjectId = signal<number>(1);
  readonly selectedFile = signal<File | null>(null);
  readonly uploading = signal<boolean>(false);
  readonly errorMessage = signal<string>('');

  ngOnInit(): void {
    this.dataService.getProyectos().subscribe((lista) => {
      this.proyectos.set(lista);
      if (lista.length > 0) {
        this.selectedProjectId.set(lista[0].id);
      }
    });
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0] ?? null;
    this.errorMessage.set('');

    if (file && !file.name.toLowerCase().endsWith('.json')) {
      this.errorMessage.set('Formato inválido: se requiere un archivo SBOM CycloneDX en formato .json');
      this.selectedFile.set(null);
      return;
    }
    this.selectedFile.set(file);
  }

  procesarArchivo(): void {
    const file = this.selectedFile();
    if (!file) return;

    this.uploading.set(true);
    this.dataService.subirSbom(this.selectedProjectId(), file).subscribe({
      next: () => {
        this.uploading.set(false);
        this.router.navigate(['/dashboard']);
      },
      error: () => {
        this.uploading.set(false);
        this.errorMessage.set('Error al procesar el archivo SBOM en el servidor.');
      }
    });
  }
}
