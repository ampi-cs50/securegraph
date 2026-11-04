package ar.edu.utn.frlp.securegraph.service;

import ar.edu.utn.frlp.securegraph.web.dto.FindingsResponseDto;
import ar.edu.utn.frlp.securegraph.web.dto.FindingsResponseDto.FindingItemDto;
import ar.edu.utn.frlp.securegraph.web.dto.FindingsResponseDto.SummaryDto;
import ar.edu.utn.frlp.securegraph.web.dto.GraphResponseDto;
import ar.edu.utn.frlp.securegraph.web.dto.GraphResponseDto.GraphEdgeDto;
import ar.edu.utn.frlp.securegraph.web.dto.GraphResponseDto.GraphNodeDto;
import ar.edu.utn.frlp.securegraph.web.dto.ReportResponseDto;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@Service
public class AnalisisService {

    private static final List<FindingItemDto> BASE_FINDINGS = List.of(
            new FindingItemDto(
                    1L,
                    "GHSA-jfh8-c2jp-5v3q",
                    "CVE-2021-44228",
                    "org.apache.logging.log4j:log4j-core",
                    "2.14.1",
                    10.0,
                    "CRITICAL",
                    true,
                    1,
                    98.5,
                    List.of(
                            "Dependencia declarada directa en el módulo principal",
                            "Severidad máxima CVSS (10.0)",
                            "Componente con alcance (scope) de ejecución en tiempo de producción"
                    )
            ),
            new FindingItemDto(
                    2L,
                    "OSV-2022-1234",
                    "CVE-2022-25857",
                    "org.yaml:snakeyaml",
                    "1.30",
                    7.5,
                    "HIGH",
                    false,
                    3,
                    62.0,
                    List.of(
                            "Dependencia transitiva de nivel 3",
                            "Severidad CVSS elevada (7.5)",
                            "Penalización de prioridad por lejanía del nodo raíz"
                    )
            ),
            new FindingItemDto(
                    3L,
                    "GHSA-rgv9-q543-rqg4",
                    "CVE-2022-42003",
                    "com.fasterxml.jackson.core:jackson-databind",
                    "2.13.4.2",
                    5.9,
                    "MEDIUM",
                    false,
                    2,
                    54.0,
                    List.of(
                            "Dependencia transitiva de nivel 2 a través de spring-boot-starter",
                            "Severidad CVSS media (5.9)",
                            "Alcance de compilación y ejecución estándar"
                    )
            )
    );

    public SummaryDto obtenerResumen(Long analisisId) {
        return new SummaryDto(25, 3, 1, 1, 1, 0);
    }

    public FindingsResponseDto obtenerHallazgos(Long analisisId, String orden, String severidad) {
        Comparator<FindingItemDto> comparator = "cvss".equalsIgnoreCase(orden)
                ? Comparator.comparingDouble(FindingItemDto::cvss).reversed()
                : Comparator.comparingDouble(FindingItemDto::secureGraphScore).reversed();

        List<FindingItemDto> filtered = BASE_FINDINGS.stream()
                .filter(f -> severidad == null || severidad.isBlank() || f.severity().equalsIgnoreCase(severidad))
                .sorted(comparator)
                .toList();

        return new FindingsResponseDto(
                analisisId,
                obtenerResumen(analisisId),
                filtered
        );
    }

    public GraphResponseDto obtenerGrafo(Long analisisId) {
        List<GraphNodeDto> nodes = List.of(
                new GraphNodeDto("mod-root", "facturacion-core-service (Root)", "MODULE", false, "NONE"),
                new GraphNodeDto("dep-1", "log4j-core:2.14.1", "DEPENDENCY", true, "CRITICAL"),
                new GraphNodeDto("dep-2", "spring-boot-starter:3.1.0", "DEPENDENCY", false, "NONE"),
                new GraphNodeDto("dep-3", "snakeyaml:1.30", "DEPENDENCY", true, "HIGH"),
                new GraphNodeDto("dep-4", "jackson-databind:2.13.4.2", "DEPENDENCY", true, "MEDIUM")
        );

        List<GraphEdgeDto> edges = List.of(
                new GraphEdgeDto("mod-root", "dep-1", "DIRECT"),
                new GraphEdgeDto("mod-root", "dep-2", "DIRECT"),
                new GraphEdgeDto("dep-2", "dep-3", "TRANSITIVE"),
                new GraphEdgeDto("dep-2", "dep-4", "TRANSITIVE")
        );

        return new GraphResponseDto(analisisId, nodes, edges);
    }

    public ReportResponseDto obtenerReporte(Long analisisId, String orden) {
        String criterio = (orden == null || orden.isBlank()) ? "score" : orden.toLowerCase();
        FindingsResponseDto hallazgos = obtenerHallazgos(analisisId, criterio, null);
        return new ReportResponseDto(
                analisisId,
                "facturacion-core-service",
                "CycloneDX-JSON",
                Instant.now(),
                criterio,
                hallazgos.summary(),
                hallazgos.findings()
        );
    }
}

