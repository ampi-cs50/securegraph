import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SecureGraphDataService } from '../../services/securegraph-data.service';
import { GraphEdge, GraphNode } from '../../models/securegraph.models';

interface PositionedNode extends GraphNode {
  x: number;
  y: number;
  width: number;
  height: number;
}

interface PositionedEdge extends GraphEdge {
  x1: number;
  y1: number;
  x2: number;
  y2: number;
  midX: number;
  midY: number;
  stroke: string;
}

@Component({
  selector: 'app-graph-view',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="graph-header">
      <div>
        <h2>Grafo de Dependencias y Rutas de Afectación (DAG)</h2>
        <p>Visualización de caminos desde el módulo raíz hasta los componentes vulnerables detectados.</p>
      </div>
      <div class="legend">
        <span class="legend-item"><i class="dot root"></i> Módulo Raíz</span>
        <span class="legend-item"><i class="dot safe"></i> Dependencia Segura</span>
        <span class="legend-item"><i class="dot critical"></i> Vulnerable (CRITICAL)</span>
        <span class="legend-item"><i class="dot high"></i> Vulnerable (HIGH/MEDIUM)</span>
      </div>
    </div>

    <div class="graph-layout">
      <section class="card canvas-box">
        <svg viewBox="0 0 820 380" class="dag-svg">
          <defs>
            <marker id="arrow" viewBox="0 0 10 10" refX="8" refY="5" markerWidth="6" markerHeight="6" orient="auto-start-reverse">
              <path d="M 0 0 L 10 5 L 0 10 z" fill="#64748b" />
            </marker>
          </defs>

          @for (edge of positionedEdges(); track edge.source + edge.target) {
            <line
              [attr.x1]="edge.x1"
              [attr.y1]="edge.y1"
              [attr.x2]="edge.x2"
              [attr.y2]="edge.y2"
              [attr.stroke]="edge.stroke"
              [attr.stroke-width]="edge.relation === 'DIRECT' ? '2.5' : '2'"
              [attr.stroke-dasharray]="edge.relation === 'TRANSITIVE' ? '5,4' : null"
              marker-end="url(#arrow)"
            />
            <text
              [attr.x]="edge.midX"
              [attr.y]="edge.midY"
              fill="#94a3b8"
              font-size="10"
              text-anchor="middle"
            >
              {{ edge.relation }}
            </text>
          }

          @for (node of positionedNodes(); track node.id) {
            <g class="node-group" (click)="seleccionarNodo(node.id)">
              <rect
                [attr.x]="node.x"
                [attr.y]="node.y"
                [attr.width]="node.width"
                [attr.height]="node.height"
                rx="10"
                [attr.fill]="getNodeFill(node)"
                [attr.stroke]="selectedNode()?.id === node.id ? '#f8fafc' : getNodeStroke(node)"
                [attr.stroke-width]="selectedNode()?.id === node.id ? '3' : '2'"
              />
              <text
                [attr.x]="node.x + node.width / 2"
                [attr.y]="node.y + 26"
                text-anchor="middle"
                fill="#f8fafc"
                font-size="11"
                font-weight="bold"
              >
                {{ formatNodeTitle(node.label) }}
              </text>
              <text
                [attr.x]="node.x + node.width / 2"
                [attr.y]="node.y + 44"
                text-anchor="middle"
                fill="#cbd5e1"
                font-size="10"
              >
                {{ formatNodeSubtitle(node) }}
              </text>
            </g>
          }
        </svg>
      </section>

      <aside class="card inspector">
        <h3>Detalle de Topología</h3>
        @if (selectedNode(); as n) {
          <div class="node-detail">
            <p><strong>ID:</strong> {{ n.id }}</p>
            <p><strong>Componente:</strong> {{ n.label }}</p>
            <p><strong>Tipo:</strong> {{ n.type }}</p>
            <p><strong>Estado:</strong> {{ n.vulnerable ? 'Afectado (' + n.severity + ')' : 'Sin hallazgos conocidos' }}</p>
          </div>
        }
        <hr />
        <h4>Aristas Registradas ({{ edges().length }})</h4>
        <ul class="edge-list">
          @for (edge of edges(); track edge.source + edge.target) {
            <li>
              <code>{{ edge.source }}</code> &rarr; <code>{{ edge.target }}</code>
              <small>({{ edge.relation }})</small>
            </li>
          }
        </ul>
      </aside>
    </div>
  `,
  styles: [`
    .graph-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 1.5rem;
    }
    .graph-header p {
      color: var(--text-secondary);
      font-size: 0.9rem;
    }
    .legend {
      display: flex;
      gap: 1rem;
      font-size: 0.8rem;
    }
    .legend-item {
      display: flex;
      align-items: center;
      gap: 0.4rem;
    }
    .dot {
      width: 10px;
      height: 10px;
      border-radius: 50%;
      display: inline-block;
    }
    .dot.root { background: #38bdf8; }
    .dot.safe { background: #64748b; }
    .dot.critical { background: #ef4444; }
    .dot.high { background: #f97316; }
    .graph-layout {
      display: grid;
      grid-template-columns: 1fr 310px;
      gap: 1.25rem;
    }
    .canvas-box {
      background: #0b1120;
      display: flex;
      align-items: center;
      justify-content: center;
    }
    .dag-svg {
      width: 100%;
      max-height: 420px;
    }
    .node-group {
      cursor: pointer;
    }
    .inspector h3, .inspector h4 {
      margin-bottom: 0.75rem;
    }
    .node-detail p {
      font-size: 0.85rem;
      margin-bottom: 0.4rem;
    }
    hr {
      border: 0;
      border-top: 1px solid var(--border-color);
      margin: 1rem 0;
    }
    .edge-list {
      list-style: none;
      display: flex;
      flex-direction: column;
      gap: 0.45rem;
      font-size: 0.8rem;
    }
  `]
})
export class GraphViewComponent implements OnInit {
  private readonly dataService = inject(SecureGraphDataService);

  readonly nodes = signal<GraphNode[]>([]);
  readonly edges = signal<GraphEdge[]>([]);
  readonly selectedNode = signal<GraphNode | null>(null);

  readonly positionedNodes = computed<PositionedNode[]>(() => {
    const rawNodes = this.nodes();
    const rawEdges = this.edges();
    const directTargets = new Set(
      rawEdges.filter((e) => e.relation === 'DIRECT').map((e) => e.target)
    );

    const roots = rawNodes.filter((n) => n.type === 'MODULE');
    const directs = rawNodes.filter((n) => n.type !== 'MODULE' && directTargets.has(n.id));
    const transitives = rawNodes.filter((n) => n.type !== 'MODULE' && !directTargets.has(n.id));

    const layoutColumn = (items: GraphNode[], x: number, width: number): PositionedNode[] => {
      const count = items.length || 1;
      const spacing = 340 / (count + 1);
      return items.map((item, idx) => ({
        ...item,
        x,
        y: Math.round(20 + spacing * (idx + 1) - 30),
        width,
        height: 60
      }));
    };

    return [
      ...layoutColumn(roots, 25, 150),
      ...layoutColumn(directs, 340, 155),
      ...layoutColumn(transitives, 635, 160)
    ];
  });

  readonly positionedEdges = computed<PositionedEdge[]>(() => {
    const nodeMap = new Map(this.positionedNodes().map((n) => [n.id, n]));
    return this.edges()
      .map((edge) => {
        const sourceNode = nodeMap.get(edge.source);
        const targetNode = nodeMap.get(edge.target);
        if (!sourceNode || !targetNode) return null;

        const x1 = sourceNode.x + sourceNode.width;
        const y1 = sourceNode.y + sourceNode.height / 2;
        const x2 = targetNode.x;
        const y2 = targetNode.y + targetNode.height / 2;

        return {
          ...edge,
          x1,
          y1,
          x2,
          y2,
          midX: (x1 + x2) / 2,
          midY: (y1 + y2) / 2 - 8,
          stroke: this.getNodeStroke(targetNode)
        };
      })
      .filter((e): e is PositionedEdge => e !== null);
  });

  ngOnInit(): void {
    this.dataService.getGrafo(42).subscribe((graph) => {
      this.nodes.set(graph.nodes);
      this.edges.set(graph.edges);
      this.selectedNode.set(graph.nodes[1] ?? graph.nodes[0] ?? null);
    });
  }

  seleccionarNodo(nodeId: string): void {
    const found = this.nodes().find((n) => n.id === nodeId) ?? null;
    this.selectedNode.set(found);
  }

  getNodeFill(node: GraphNode): string {
    if (node.type === 'MODULE') return '#0284c7';
    switch (node.severity) {
      case 'CRITICAL':
        return '#450a0a';
      case 'HIGH':
        return '#431407';
      case 'MEDIUM':
        return '#422006';
      default:
        return '#1e293b';
    }
  }

  getNodeStroke(node: GraphNode): string {
    if (node.type === 'MODULE') return '#38bdf8';
    switch (node.severity) {
      case 'CRITICAL':
        return '#ef4444';
      case 'HIGH':
        return '#f97316';
      case 'MEDIUM':
        return '#eab308';
      default:
        return '#475569';
    }
  }

  formatNodeTitle(label: string): string {
    const clean = label.replace(' (Root)', '');
    return clean.length > 22 ? clean.slice(0, 20) + '...' : clean;
  }

  formatNodeSubtitle(node: GraphNode): string {
    if (node.type === 'MODULE') return 'MODULE (Root)';
    return node.vulnerable ? `${node.severity} (Vulnerable)` : 'Sin hallazgos';
  }
}
