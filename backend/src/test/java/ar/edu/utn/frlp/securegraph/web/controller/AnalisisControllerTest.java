package ar.edu.utn.frlp.securegraph.web.controller;

import ar.edu.utn.frlp.securegraph.domain.entity.Analisis;
import ar.edu.utn.frlp.securegraph.domain.entity.Componente;
import ar.edu.utn.frlp.securegraph.domain.entity.HallazgoVulnerabilidad;
import ar.edu.utn.frlp.securegraph.domain.entity.Proyecto;
import ar.edu.utn.frlp.securegraph.repository.AnalisisRepository;
import ar.edu.utn.frlp.securegraph.repository.ComponenteRepository;
import ar.edu.utn.frlp.securegraph.repository.HallazgoVulnerabilidadRepository;
import ar.edu.utn.frlp.securegraph.repository.HallazgoVulnerabilidadRepository.ConteoSeveridad;
import ar.edu.utn.frlp.securegraph.repository.RelacionDependenciaRepository;
import ar.edu.utn.frlp.securegraph.service.AnalisisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AnalisisController.class)
@Import(AnalisisService.class)
class AnalisisControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private AnalisisRepository analyses;
    @MockBean private ComponenteRepository components;
    @MockBean private HallazgoVulnerabilidadRepository findings;
    @MockBean private RelacionDependenciaRepository dependencies;

    @BeforeEach
    void analysisExists() {
        var project = new Proyecto();
        project.setNombre("Inventory API");
        var analysis = new Analisis();
        analysis.setProyecto(project);
        analysis.setTotalComponentes(2);
        when(analyses.findOneById(42L)).thenReturn(Optional.of(analysis));
    }

    @Test
    void shouldReturnSummaryForAnalysis() throws Exception {
        var group = mock(ConteoSeveridad.class);
        when(group.getNivel()).thenReturn("CRITICAL");
        when(group.getTotal()).thenReturn(1L);
        when(findings.contarPorSeveridad(42L)).thenReturn(List.of(group));
        mockMvc.perform(get("/api/analisis/42/resumen"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components").value(2))
                .andExpect(jsonPath("$.vulnerabilities").value(1))
                .andExpect(jsonPath("$.critical").value(1));
    }

    @Test
    void shouldReturnPrioritizedFindingsForAnalysis() throws Exception {
        when(findings.buscarConComponente(42L)).thenReturn(List.of(
                finding(11, "HIGH", "8.0", "66.0"),
                finding(12, "CRITICAL", "9.8", "70.0")));
        mockMvc.perform(get("/api/analisis/42/hallazgos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analysisId").value(42))
                .andExpect(jsonPath("$.findings[0].id").value(12))
                .andExpect(jsonPath("$.findings[0].secureGraphScore").value(70.0))
                .andExpect(jsonPath("$.findings[0].reasons.length()").value(4));
    }

    @Test
    void shouldReturnGraphForAnalysis() throws Exception {
        mockMvc.perform(get("/api/analisis/42/grafo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analysisId").value(42))
                .andExpect(jsonPath("$.nodes").isArray())
                .andExpect(jsonPath("$.edges").isArray());
    }

    @Test
    void shouldReturnConsolidatedReportForAnalysis() throws Exception {
        mockMvc.perform(get("/api/analisis/42/reporte").param("orden", "CVSS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analysisId").value(42))
                .andExpect(jsonPath("$.projectName").value("Inventory API"))
                .andExpect(jsonPath("$.sbomFormat").value("CycloneDX-JSON"))
                .andExpect(jsonPath("$.orderingCriterion").value("cvss"))
                .andExpect(jsonPath("$.generatedAt").value(endsWith("Z")))
                .andExpect(jsonPath("$.findings").isArray());
    }

    @Test
    void ordersNullableCvssLastAndKeepsTheSummaryUnfiltered() throws Exception {
        when(findings.buscarConComponente(42L)).thenReturn(List.of(
                finding(13, "HIGH", null, "85.0"),
                finding(11, "HIGH", "8.0", "66.0"),
                finding(12, "CRITICAL", "9.8", "70.0")));
        mockMvc.perform(get("/api/analisis/42/hallazgos").param("orden", "CvSs").param("severidad", "high"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.vulnerabilities").value(3))
                .andExpect(jsonPath("$.findings.length()").value(2))
                .andExpect(jsonPath("$.findings[0].id").value(11))
                .andExpect(jsonPath("$.findings[1].id").value(13))
                .andExpect(jsonPath("$.findings[1].cvss").value(nullValue()))
                .andExpect(jsonPath("$.findings[1].depth").value(nullValue()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"resumen", "hallazgos", "grafo", "reporte"})
    void returnsNotFoundUsingTheStandardError(String endpoint) throws Exception {
        String path = "/api/analisis/99/" + endpoint;
        mockMvc.perform(get(path))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").isString())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.path").value(path));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/analisis/x/resumen", "/api/analisis/0/grafo",
            "/api/analisis/42/hallazgos?orden=date", "/api/analisis/42/hallazgos?severidad=urgent",
            "/api/analisis/42/reporte?orden=date"})
    void rejectsInvalidIdsAndParameters(String url) throws Exception {
        mockMvc.perform(get(url))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$.timestamp").isString())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").isString())
                .andExpect(jsonPath("$.path").value(url.split("\\?", 2)[0]));
    }

    private static HallazgoVulnerabilidad finding(long id, String severity, String cvss, String score) {
        var component = new Componente();
        component.setGroupId("example");
        component.setArtifactId("library");
        var finding = new HallazgoVulnerabilidad();
        finding.setId(id);
        finding.setComponente(component);
        finding.setOsvId("OSV-" + id);
        finding.setNivelSeveridad(severity);
        finding.setSeveridadCvss(cvss == null ? null : new BigDecimal(cvss));
        finding.setScorePrioridad(new BigDecimal(score));
        finding.setRazonesPrioridad("Severidad\nProfundidad\nRelación\nAlcance");
        return finding;
    }
}
