package ar.edu.utn.frlp.securegraph.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@EnabledIfEnvironmentVariable(named = "SECUREGRAPH_TEST_POSTGRES_URL", matches = "jdbc:postgresql:.*")
class AnalysisQueryPostgresTest {

    @Autowired private JdbcTemplate jdbc;
    @Autowired private MockMvc mvc;
    private long analysis;
    private long emptyAnalysis;

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {
        DependencyGraphPostgresTest.configureDatabase(registry);
    }

    @BeforeEach
    void populateAnalyses() {
        analysis = analysis("Inventory API", 3);
        emptyAnalysis = analysis("Empty API", 0);
        long other = analysis("Other project", 1);
        long root = component(analysis, "root", true, false);
        long direct = component(analysis, "direct", false, true);
        long transitive = component(analysis, "transitive", false, false);
        long disconnected = component(analysis, "disconnected", false, false);
        long foreign = component(other, "foreign", false, true);
        edge(analysis, root, direct, false);
        edge(analysis, direct, transitive, true);
        edge(analysis, root, foreign, false);
        finding(analysis, direct, "OSV-HIGH", "HIGH", "8.0", 1, "80.0");
        finding(analysis, direct, "OSV-LOW", "LOW", "3.0", 1, "30.0");
        finding(analysis, transitive, "OSV-CRITICAL", "CRITICAL", "9.8", 2, "76.0");
        finding(analysis, disconnected, "OSV-UNKNOWN", "UNKNOWN", null, null, "25.4");
        finding(other, foreign, "OSV-OTHER", "CRITICAL", "10.0", 1, "100.0");
        finding(analysis, foreign, "OSV-CROSS", "CRITICAL", "10.0", 1, "100.0");
        finding(other, direct, "OSV-CROSS-REVERSE", "CRITICAL", "10.0", 1, "100.0");
    }

    @Test
    void readsSummaryFindingsAndReportFromTheRequestedAnalysis() throws Exception {
        mvc.perform(get(path("resumen"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.components").value(3))
                .andExpect(jsonPath("$.vulnerabilities").value(4))
                .andExpect(jsonPath("$.critical").value(1))
                .andExpect(jsonPath("$.high").value(1))
                .andExpect(jsonPath("$.medium").value(0))
                .andExpect(jsonPath("$.low").value(1));
        mvc.perform(get(path("hallazgos"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.findings.length()").value(4))
                .andExpect(jsonPath("$.findings[0].vulnerabilityId").value("OSV-HIGH"))
                .andExpect(jsonPath("$.findings[0].component").value("example:direct"))
                .andExpect(jsonPath("$.findings[0].secureGraphScore").value(80))
                .andExpect(jsonPath("$.findings[0].reasons.length()").value(4));
        mvc.perform(get(path("hallazgos")).param("severidad", "UNKNOWN")).andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.vulnerabilities").value(4))
                .andExpect(jsonPath("$.findings.length()").value(1))
                .andExpect(jsonPath("$.findings[0].cvss").value(nullValue()))
                .andExpect(jsonPath("$.findings[0].depth").value(nullValue()));
        mvc.perform(get(path("reporte")).param("orden", "cvss")).andExpect(status().isOk())
                .andExpect(jsonPath("$.projectName").value("Inventory API"))
                .andExpect(jsonPath("$.sbomFormat").value("CycloneDX-JSON"))
                .andExpect(jsonPath("$.generatedAt").isString())
                .andExpect(jsonPath("$.orderingCriterion").value("cvss"))
                .andExpect(jsonPath("$.findings.length()").value(4))
                .andExpect(jsonPath("$.findings[0].vulnerabilityId").value("OSV-CRITICAL"))
                .andExpect(jsonPath("$.findings[3].vulnerabilityId").value("OSV-UNKNOWN"));
    }

    @Test
    void readsGraphWithCorrectNodeSeveritiesAndExcludesCrossAnalysisEdges() throws Exception {
        mvc.perform(get(path("grafo"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.nodes.length()").value(4))
                .andExpect(jsonPath("$.nodes[0].id").value("root"))
                .andExpect(jsonPath("$.nodes[0].type").value("MODULE"))
                .andExpect(jsonPath("$.nodes[0].vulnerable").value(false))
                .andExpect(jsonPath("$.nodes[1].severity").value("HIGH"))
                .andExpect(jsonPath("$.nodes[1].vulnerable").value(true))
                .andExpect(jsonPath("$.nodes[3].severity").value("UNKNOWN"))
                .andExpect(jsonPath("$.edges.length()").value(2))
                .andExpect(jsonPath("$.edges[0].source").value("root"))
                .andExpect(jsonPath("$.edges[0].target").value("direct"))
                .andExpect(jsonPath("$.edges[0].relation").value("DIRECT"))
                .andExpect(jsonPath("$.edges[1].relation").value("TRANSITIVE"));
    }

    @Test
    void distinguishesEmptyAnalysesFromMissingAnalyses() throws Exception {
        mvc.perform(get("/api/analisis/{id}/hallazgos", emptyAnalysis)).andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.vulnerabilities").value(0))
                .andExpect(jsonPath("$.findings").isEmpty());
        mvc.perform(get("/api/analisis/{id}/grafo", emptyAnalysis)).andExpect(status().isOk())
                .andExpect(jsonPath("$.nodes").isEmpty())
                .andExpect(jsonPath("$.edges").isEmpty());
        mvc.perform(get("/api/analisis/{id}/resumen", Long.MAX_VALUE)).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    private String path(String endpoint) {
        return "/api/analisis/" + analysis + "/" + endpoint;
    }

    private long analysis(String project, int components) {
        Long projectId = jdbc.queryForObject("INSERT INTO proyecto(nombre) VALUES (?) RETURNING id", Long.class, project);
        return jdbc.queryForObject("""
                INSERT INTO analisis(proyecto_id, total_componentes) VALUES (?, ?) RETURNING id
                """, Long.class, projectId, components);
    }

    private long component(long analysisId, String ref, boolean root, boolean direct) {
        return jdbc.queryForObject("""
                INSERT INTO componente(analisis_id, bom_ref, group_id, artifact_id, version, es_raiz, es_directa)
                VALUES (?, ?, 'example', ?, '1.0', ?, ?) RETURNING id
                """, Long.class, analysisId, ref, ref, root, direct);
    }

    private void edge(long analysisId, long parent, long child, boolean transitive) {
        jdbc.update("""
                INSERT INTO relacion_dependencia(analisis_id, padre_componente_id, hijo_componente_id, tipo_transitiva)
                VALUES (?, ?, ?, ?)
                """, analysisId, parent, child, transitive);
    }

    private void finding(long analysisId, long component, String id, String severity, String cvss,
                         Integer depth, String score) {
        jdbc.update("""
                INSERT INTO hallazgo_vulnerabilidad(analisis_id, componente_id, osv_id, nivel_severidad,
                                                    severidad_cvss, profundidad, score_prioridad, razones_prioridad)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """, analysisId, component, id, severity, cvss == null ? null : new BigDecimal(cvss), depth,
                new BigDecimal(score), "Severidad\nProfundidad\nRelación\nAlcance");
    }
}
