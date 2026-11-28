package ar.edu.utn.frlp.securegraph.service;

import ar.edu.utn.frlp.securegraph.domain.entity.Analisis;
import ar.edu.utn.frlp.securegraph.domain.entity.Componente;
import ar.edu.utn.frlp.securegraph.domain.entity.HallazgoVulnerabilidad;
import ar.edu.utn.frlp.securegraph.repository.AnalisisRepository;
import ar.edu.utn.frlp.securegraph.repository.ComponenteRepository;
import ar.edu.utn.frlp.securegraph.repository.HallazgoVulnerabilidadRepository;
import ar.edu.utn.frlp.securegraph.repository.RelacionDependenciaRepository;
import ar.edu.utn.frlp.securegraph.web.dto.FindingsResponseDto;
import ar.edu.utn.frlp.securegraph.web.dto.FindingsResponseDto.FindingItemDto;
import ar.edu.utn.frlp.securegraph.web.dto.FindingsResponseDto.SummaryDto;
import ar.edu.utn.frlp.securegraph.web.dto.GraphResponseDto;
import ar.edu.utn.frlp.securegraph.web.dto.GraphResponseDto.GraphEdgeDto;
import ar.edu.utn.frlp.securegraph.web.dto.GraphResponseDto.GraphNodeDto;
import ar.edu.utn.frlp.securegraph.web.dto.ReportResponseDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class AnalisisService {

    private final AnalisisRepository analyses;
    private final ComponenteRepository components;
    private final HallazgoVulnerabilidadRepository findings;
    private final RelacionDependenciaRepository dependencies;

    public AnalisisService(AnalisisRepository analyses, ComponenteRepository components,
                           HallazgoVulnerabilidadRepository findings, RelacionDependenciaRepository dependencies) {
        this.analyses = analyses;
        this.components = components;
        this.findings = findings;
        this.dependencies = dependencies;
    }

    public SummaryDto obtenerResumen(Long analisisId) {
        Analisis analysis = requireAnalysis(analisisId);
        var counts = new EnumMap<Severity, Integer>(Severity.class);
        for (var group : findings.contarPorSeveridad(analisisId)) {
            counts.merge(Severity.stored(group.getNivel()), Math.toIntExact(group.getTotal()), Integer::sum);
        }
        return summary(analysis, counts);
    }

    public FindingsResponseDto obtenerHallazgos(Long analisisId, String orden, String severidad) {
        Ordering ordering = Ordering.parse(orden);
        Severity filter = Severity.filter(severidad);
        Analisis analysis = requireAnalysis(analisisId);
        List<HallazgoVulnerabilidad> rows = findings.buscarConComponente(analisisId);
        return new FindingsResponseDto(analisisId, summary(analysis, rows), items(rows, ordering, filter));
    }

    public GraphResponseDto obtenerGrafo(Long analisisId) {
        requireAnalysis(analisisId);
        Map<Long, Severity> componentSeverity = new HashMap<>();
        for (var finding : findings.buscarSeveridadesDeComponentes(analisisId)) {
            componentSeverity.merge(finding.getComponentId(), Severity.stored(finding.getNivel()),
                    (a, b) -> a.compareTo(b) >= 0 ? a : b);
        }
        List<GraphNodeDto> nodes = components.findByAnalisisIdOrderByIdAsc(analisisId).stream()
                .map(component -> graphNode(component, componentSeverity.get(component.getId())))
                .toList();
        List<GraphEdgeDto> edges = dependencies.buscarAristas(analisisId).stream()
                .map(edge -> new GraphEdgeDto(edge.getSource(), edge.getTarget(),
                        edge.getTransitive() ? "TRANSITIVE" : "DIRECT"))
                .toList();
        return new GraphResponseDto(analisisId, nodes, edges);
    }

    public ReportResponseDto obtenerReporte(Long analisisId, String orden) {
        Ordering ordering = Ordering.parse(orden);
        Analisis analysis = requireAnalysis(analisisId);
        List<HallazgoVulnerabilidad> rows = findings.buscarConComponente(analisisId);
        return new ReportResponseDto(analisisId, analysis.getProyecto().getNombre(), analysis.getSbomFormato(),
                Instant.now(), ordering.name().toLowerCase(Locale.ROOT), summary(analysis, rows),
                items(rows, ordering, null));
    }

    private Analisis requireAnalysis(Long id) {
        if (id == null || id <= 0) {
            throw new InvalidAnalysisQueryException("El identificador del análisis debe ser un entero positivo.");
        }
        return analyses.findOneById(id).orElseThrow(AnalysisNotFoundException::new);
    }

    private static SummaryDto summary(Analisis analysis, List<HallazgoVulnerabilidad> rows) {
        var counts = new EnumMap<Severity, Integer>(Severity.class);
        rows.forEach(row -> counts.merge(Severity.stored(row.getNivelSeveridad()), 1, Integer::sum));
        return summary(analysis, counts);
    }

    private static SummaryDto summary(Analisis analysis, Map<Severity, Integer> counts) {
        int total = counts.values().stream().mapToInt(Integer::intValue).sum();
        return new SummaryDto(analysis.getTotalComponentes(), total,
                counts.getOrDefault(Severity.CRITICAL, 0), counts.getOrDefault(Severity.HIGH, 0),
                counts.getOrDefault(Severity.MEDIUM, 0), counts.getOrDefault(Severity.LOW, 0));
    }

    private static List<FindingItemDto> items(List<HallazgoVulnerabilidad> rows, Ordering ordering, Severity filter) {
        return rows.stream()
                .filter(row -> filter == null || filter == Severity.stored(row.getNivelSeveridad()))
                .sorted(ordering.comparator.thenComparing(HallazgoVulnerabilidad::getId))
                .map(AnalisisService::findingItem)
                .toList();
    }

    private static FindingItemDto findingItem(HallazgoVulnerabilidad finding) {
        Componente component = finding.getComponente();
        BigDecimal cvss = finding.getSeveridadCvss();
        String reasons = finding.getRazonesPrioridad();
        String coordinates = component.getGroupId() == null ? component.getArtifactId()
                : component.getGroupId() + ":" + component.getArtifactId();
        return new FindingItemDto(finding.getId(), finding.getOsvId(), finding.getCveId(), coordinates,
                component.getVersion(), cvss == null ? null : cvss.doubleValue(),
                Severity.stored(finding.getNivelSeveridad()).name(), Boolean.TRUE.equals(component.getEsDirecta()),
                finding.getProfundidad(), finding.getScorePrioridad().doubleValue(),
                reasons == null ? List.of() : reasons.lines().filter(line -> !line.isBlank()).toList());
    }

    private static GraphNodeDto graphNode(Componente component, Severity severity) {
        String label = component.getVersion() == null ? component.getArtifactId()
                : component.getArtifactId() + ":" + component.getVersion();
        return new GraphNodeDto(component.getBomRef(), label, component.isRaiz() ? "MODULE" : "DEPENDENCY",
                severity != null, severity == null ? Severity.NONE.name() : severity.name());
    }

    private enum Ordering {
        SCORE(Comparator.comparing(HallazgoVulnerabilidad::getScorePrioridad).reversed()),
        CVSS(Comparator.comparing(HallazgoVulnerabilidad::getSeveridadCvss,
                Comparator.nullsLast(Comparator.reverseOrder())));

        private final Comparator<HallazgoVulnerabilidad> comparator;

        Ordering(Comparator<HallazgoVulnerabilidad> comparator) {
            this.comparator = comparator;
        }

        static Ordering parse(String value) {
            if (value == null || value.isBlank()) {
                return SCORE;
            }
            try {
                return valueOf(value.strip().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException exception) {
                throw new InvalidAnalysisQueryException("El parámetro orden debe ser score o cvss.");
            }
        }
    }

    private enum Severity {
        NONE, UNKNOWN, LOW, MEDIUM, HIGH, CRITICAL;

        static Severity filter(String value) {
            if (value == null || value.isBlank()) {
                return null;
            }
            try {
                return valueOf(value.strip().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException exception) {
                throw new InvalidAnalysisQueryException("El parámetro severidad no contiene un nivel válido.");
            }
        }

        static Severity stored(String value) {
            try {
                return valueOf(value);
            } catch (IllegalArgumentException exception) {
                return UNKNOWN;
            }
        }
    }

    public static class InvalidAnalysisQueryException extends RuntimeException {
        public InvalidAnalysisQueryException(String message) {
            super(message);
        }
    }

    public static class AnalysisNotFoundException extends RuntimeException {
    }
}
