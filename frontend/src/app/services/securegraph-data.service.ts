import { Injectable, signal } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { delay } from 'rxjs/operators';
import {
  AnalysisSummary,
  CreateProyectoRequest,
  FindingsResponse,
  GraphResponse,
  HealthResponse,
  Proyecto,
  ReportResponse
} from '../models/securegraph.models';

@Injectable({
  providedIn: 'root'
})
export class SecureGraphDataService {
  private readonly baseUrl = 'http://localhost:8080/api';

  readonly useMockMode = signal<boolean>(true);

  private mockProjects: Proyecto[] = [
    {
      id: 1,
      nombre: 'facturacion-core-service',
      descripcion: 'Microservicio principal de facturación electrónica (Java 21 / Maven)',
      createdAt: '2026-11-04T14:20:00Z',
      ultimoAnalisisId: 42,
      totalAnalisis: 2
    },
    {
      id: 2,
      nombre: 'pagos-gateway-api',
      descripcion: 'Integrador de medios de pago y conciliación bancaria',
      createdAt: '2026-11-03T10:15:00Z',
      ultimoAnalisisId: 41,
      totalAnalisis: 1
    }
  ];

  private readonly mockFindingsResponse: FindingsResponse = {
    analysisId: 42,
    summary: {
      components: 25,
      vulnerabilities: 3,
      critical: 1,
      high: 1,
      medium: 1,
      low: 0
    },
    findings: [
      {
        id: 1,
        vulnerabilityId: 'GHSA-jfh8-c2jp-5v3q',
        cveId: 'CVE-2021-44228',
        component: 'org.apache.logging.log4j:log4j-core',
        version: '2.14.1',
        cvss: 10.0,
        severity: 'CRITICAL',
        direct: true,
        depth: 1,
        secureGraphScore: 98.5,
        reasons: [
          'Dependencia declarada directa en el módulo principal',
          'Severidad máxima CVSS (10.0)',
          'Componente con alcance (scope) de ejecución en tiempo de producción'
        ]
      },
      {
        id: 2,
        vulnerabilityId: 'OSV-2022-1234',
        cveId: 'CVE-2022-25857',
        component: 'org.yaml:snakeyaml',
        version: '1.30',
        cvss: 7.5,
        severity: 'HIGH',
        direct: false,
        depth: 3,
        secureGraphScore: 62.0,
        reasons: [
          'Dependencia transitiva de nivel 3',
          'Severidad CVSS elevada (7.5)',
          'Penalización de prioridad por lejanía del nodo raíz'
        ]
      },
      {
        id: 3,
        vulnerabilityId: 'GHSA-rgv9-q543-rqg4',
        cveId: 'CVE-2022-42003',
        component: 'com.fasterxml.jackson.core:jackson-databind',
        version: '2.13.4.2',
        cvss: 5.9,
        severity: 'MEDIUM',
        direct: false,
        depth: 2,
        secureGraphScore: 54.0,
        reasons: [
          'Dependencia transitiva de nivel 2 a través de spring-boot-starter',
          'Severidad CVSS media (5.9)',
          'Alcance de compilación y ejecución estándar'
        ]
      }
    ]
  };

  private readonly mockGraphResponse: GraphResponse = {
    analysisId: 42,
    nodes: [
      { id: 'mod-root', label: 'facturacion-core-service (Root)', type: 'MODULE', vulnerable: false, severity: 'NONE' },
      { id: 'dep-1', label: 'log4j-core:2.14.1', type: 'DEPENDENCY', vulnerable: true, severity: 'CRITICAL' },
      { id: 'dep-2', label: 'spring-boot-starter:3.1.0', type: 'DEPENDENCY', vulnerable: false, severity: 'NONE' },
      { id: 'dep-3', label: 'snakeyaml:1.30', type: 'DEPENDENCY', vulnerable: true, severity: 'HIGH' },
      { id: 'dep-4', label: 'jackson-databind:2.13.4.2', type: 'DEPENDENCY', vulnerable: true, severity: 'MEDIUM' }
    ],
    edges: [
      { source: 'mod-root', target: 'dep-1', relation: 'DIRECT' },
      { source: 'mod-root', target: 'dep-2', relation: 'DIRECT' },
      { source: 'dep-2', target: 'dep-3', relation: 'TRANSITIVE' },
      { source: 'dep-2', target: 'dep-4', relation: 'TRANSITIVE' }
    ]
  };

  constructor(private readonly http: HttpClient) {}

  toggleMode(): void {
    this.useMockMode.update((v) => !v);
  }

  checkHealth(): Observable<HealthResponse> {
    if (this.useMockMode()) {
      return of({
        status: 'UP (MOCK)',
        service: 'securegraph-mock-service',
        database: 'Simulado en Memoria',
        timestamp: new Date().toISOString()
      });
    }
    return this.http.get<HealthResponse>(`${this.baseUrl}/health`);
  }

  getProyectos(): Observable<Proyecto[]> {
    if (this.useMockMode()) {
      return of([...this.mockProjects]).pipe(delay(150));
    }
    return this.http.get<Proyecto[]>(`${this.baseUrl}/proyectos`);
  }

  crearProyecto(payload: CreateProyectoRequest): Observable<Proyecto> {
    if (this.useMockMode()) {
      const nuevo: Proyecto = {
        id: this.mockProjects.length + 1,
        nombre: payload.nombre,
        descripcion: payload.descripcion,
        createdAt: new Date().toISOString(),
        ultimoAnalisisId: null,
        totalAnalisis: 0
      };
      this.mockProjects = [nuevo, ...this.mockProjects];
      return of(nuevo).pipe(delay(200));
    }
    return this.http.post<Proyecto>(`${this.baseUrl}/proyectos`, payload);
  }

  subirSbom(proyectoId: number, file: File): Observable<{ projectId: number; analysisId: number; status: string }> {
    if (this.useMockMode()) {
      return of({
        projectId: proyectoId,
        analysisId: 42,
        status: 'COMPLETED'
      }).pipe(delay(400));
    }
    const formData = new FormData();
    formData.append('file', file);
    return this.http.post<{ projectId: number; analysisId: number; status: string }>(
      `${this.baseUrl}/proyectos/${proyectoId}/analisis`,
      formData
    );
  }

  getResumen(analysisId: number): Observable<AnalysisSummary> {
    if (this.useMockMode()) {
      return of(this.mockFindingsResponse.summary).pipe(delay(100));
    }
    return this.http.get<AnalysisSummary>(`${this.baseUrl}/analisis/${analysisId}/resumen`);
  }

  getHallazgos(analysisId: number, orden: 'score' | 'cvss' = 'score', severidad = ''): Observable<FindingsResponse> {
    if (this.useMockMode()) {
      const items = [...this.mockFindingsResponse.findings]
        .filter((f) => !severidad || f.severity === severidad)
        .sort((a, b) => (orden === 'cvss' ? b.cvss - a.cvss : b.secureGraphScore - a.secureGraphScore));

      return of({
        analysisId,
        summary: this.mockFindingsResponse.summary,
        findings: items
      }).pipe(delay(150));
    }

    let params = new HttpParams().set('orden', orden);
    if (severidad) {
      params = params.set('severidad', severidad);
    }
    return this.http.get<FindingsResponse>(`${this.baseUrl}/analisis/${analysisId}/hallazgos`, { params });
  }

  getGrafo(analysisId: number): Observable<GraphResponse> {
    if (this.useMockMode()) {
      return of(this.mockGraphResponse).pipe(delay(150));
    }
    return this.http.get<GraphResponse>(`${this.baseUrl}/analisis/${analysisId}/grafo`);
  }

  getReporte(analysisId: number, orden: 'score' | 'cvss' = 'score'): Observable<ReportResponse> {
    if (this.useMockMode()) {
      const sortedFindings = [...this.mockFindingsResponse.findings].sort((a, b) =>
        orden === 'cvss' ? b.cvss - a.cvss : b.secureGraphScore - a.secureGraphScore
      );
      return of({
        analysisId,
        projectName: 'facturacion-core-service',
        sbomFormat: 'CycloneDX-JSON',
        generatedAt: new Date().toISOString(),
        orderingCriterion: orden,
        summary: this.mockFindingsResponse.summary,
        findings: sortedFindings
      }).pipe(delay(150));
    }
    const params = new HttpParams().set('orden', orden);
    return this.http.get<ReportResponse>(`${this.baseUrl}/analisis/${analysisId}/reporte`, { params });
  }
}
