export type SeverityLevel = 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW' | 'NONE';

export interface HealthResponse {
  status: string;
  service: string;
  database: string;
  timestamp: string;
}

export interface Proyecto {
  id: number;
  nombre: string;
  descripcion: string;
  createdAt: string;
  ultimoAnalisisId: number | null;
  totalAnalisis: number;
}

export interface CreateProyectoRequest {
  nombre: string;
  descripcion: string;
}

export interface AnalysisSummary {
  components: number;
  vulnerabilities: number;
  critical: number;
  high: number;
  medium: number;
  low: number;
}

export interface FindingItem {
  id: number;
  vulnerabilityId: string;
  cveId: string;
  component: string;
  version: string;
  cvss: number;
  severity: SeverityLevel;
  direct: boolean;
  depth: number;
  secureGraphScore: number;
  reasons: string[];
}

export interface FindingsResponse {
  analysisId: number;
  summary: AnalysisSummary;
  findings: FindingItem[];
}

export interface GraphNode {
  id: string;
  label: string;
  type: 'MODULE' | 'DEPENDENCY';
  vulnerable: boolean;
  severity?: SeverityLevel;
}

export interface GraphEdge {
  source: string;
  target: string;
  relation: 'DIRECT' | 'TRANSITIVE';
}

export interface GraphResponse {
  analysisId: number;
  nodes: GraphNode[];
  edges: GraphEdge[];
}

export interface ReportResponse {
  analysisId: number;
  projectName: string;
  sbomFormat: string;
  generatedAt: string;
  orderingCriterion: string;
  summary: AnalysisSummary;
  findings: FindingItem[];
}
