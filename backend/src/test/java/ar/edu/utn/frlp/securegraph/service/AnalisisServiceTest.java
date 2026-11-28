package ar.edu.utn.frlp.securegraph.service;

import ar.edu.utn.frlp.securegraph.domain.entity.Analisis;
import ar.edu.utn.frlp.securegraph.domain.entity.Componente;
import ar.edu.utn.frlp.securegraph.domain.entity.HallazgoVulnerabilidad;
import ar.edu.utn.frlp.securegraph.repository.AnalisisRepository;
import ar.edu.utn.frlp.securegraph.repository.ComponenteRepository;
import ar.edu.utn.frlp.securegraph.repository.HallazgoVulnerabilidadRepository;
import ar.edu.utn.frlp.securegraph.repository.HallazgoVulnerabilidadRepository.SeveridadComponente;
import ar.edu.utn.frlp.securegraph.repository.RelacionDependenciaRepository;
import ar.edu.utn.frlp.securegraph.web.dto.FindingsResponseDto.FindingItemDto;
import ar.edu.utn.frlp.securegraph.web.dto.GraphResponseDto.GraphNodeDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AnalisisServiceTest {

    private final AnalisisRepository analyses = mock(AnalisisRepository.class);
    private final ComponenteRepository components = mock(ComponenteRepository.class);
    private final HallazgoVulnerabilidadRepository findings = mock(HallazgoVulnerabilidadRepository.class);
    private final AnalisisService service = new AnalisisService(analyses, components, findings,
            mock(RelacionDependenciaRepository.class));

    @BeforeEach
    void analysisExists() {
        when(analyses.findOneById(7L)).thenReturn(Optional.of(new Analisis()));
    }

    @Test
    void breaksScoreTiesByIdAndDistinguishesZeroCvssFromNull() {
        when(findings.buscarConComponente(7L)).thenReturn(List.of(finding(2L, "0.0"), finding(1L, null)));

        assertThat(service.obtenerHallazgos(7L, "score", null).findings())
                .extracting(FindingItemDto::id).containsExactly(1L, 2L);
        assertThat(service.obtenerHallazgos(7L, "cvss", null).findings())
                .extracting(FindingItemDto::id, FindingItemDto::cvss)
                .containsExactly(tuple(2L, 0.0), tuple(1L, null));
    }

    @Test
    void distinguishesUnratedAffectedNodesFromUnaffectedNodes() {
        when(components.findByAnalisisIdOrderByIdAsc(7L)).thenReturn(List.of(
                component(1L, "root", true), component(2L, "affected", false),
                component(3L, "unrated", false)));
        var severities = List.of(severity(2L, "LOW"), severity(2L, "CRITICAL"),
                severity(2L, "MEDIUM"), severity(3L, "UNKNOWN"));
        when(findings.buscarSeveridadesDeComponentes(7L)).thenReturn(severities);

        assertThat(service.obtenerGrafo(7L).nodes())
                .extracting(GraphNodeDto::id, GraphNodeDto::type, GraphNodeDto::vulnerable, GraphNodeDto::severity)
                .containsExactly(tuple("root", "MODULE", false, "NONE"),
                        tuple("affected", "DEPENDENCY", true, "CRITICAL"),
                        tuple("unrated", "DEPENDENCY", true, "UNKNOWN"));
    }

    private static HallazgoVulnerabilidad finding(long id, String cvss) {
        var finding = new HallazgoVulnerabilidad();
        finding.setId(id);
        finding.setComponente(component(id, "library", false));
        finding.setSeveridadCvss(cvss == null ? null : new BigDecimal(cvss));
        finding.setNivelSeveridad("UNKNOWN");
        return finding;
    }

    private static Componente component(long id, String ref, boolean root) {
        var component = new Componente();
        component.setId(id);
        component.setBomRef(ref);
        component.setArtifactId(ref);
        component.setRaiz(root);
        return component;
    }

    private static SeveridadComponente severity(long id, String level) {
        var projection = mock(SeveridadComponente.class);
        when(projection.getComponentId()).thenReturn(id);
        when(projection.getNivel()).thenReturn(level);
        return projection;
    }
}
