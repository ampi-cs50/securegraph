package ar.edu.utn.frlp.securegraph.service;

import ar.edu.utn.frlp.securegraph.service.DependencyGraphService.ComponentDepth;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EnabledIfEnvironmentVariable(named = "SECUREGRAPH_TEST_POSTGRES_URL", matches = "jdbc:postgresql:.*")
@Transactional
class DependencyGraphPostgresTest {

    @Autowired
    private DependencyGraphService graph;

    @Autowired
    private SbomImportService importer;

    @Autowired
    private JdbcTemplate jdbc;

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry properties) {
        properties.add("spring.datasource.url", () -> System.getenv("SECUREGRAPH_TEST_POSTGRES_URL"));
        properties.add("spring.datasource.username",
                () -> System.getenv().getOrDefault("SECUREGRAPH_TEST_POSTGRES_USER", "securegraph_user"));
        properties.add("spring.datasource.password",
                () -> System.getenv().getOrDefault("SECUREGRAPH_TEST_POSTGRES_PASSWORD", "securegraph_pass"));
    }

    @Test
    void traversesThePersistedSampleSbom() throws Exception {
        long project = project();
        Path sample = Path.of("..", "samples", "sample-maven-cyclonedx.json");
        var imported = importer.importSbom(project, new MockMultipartFile(
                "file", sample.getFileName().toString(), "application/json", Files.readAllBytes(sample)));

        assertThat(graph.findMinimumDepths(imported.analysisId()))
                .extracting(ComponentDepth::depth)
                .containsExactly(0, 1, 1, 2, 2, 2);
    }

    @Test
    void usesTheShortestPathAndStopsAtPreviouslyVisitedNodes() {
        long analysis = analysis();
        long shortHop = node(analysis, "short-hop", false);
        long root = node(analysis, "root", true);
        long longHop = node(analysis, "long-hop", false);
        long extraHop = node(analysis, "extra-hop", false);
        long shared = node(analysis, "shared", false);
        long disconnected = node(analysis, "disconnected", false);
        edge(analysis, root, shortHop);
        edge(analysis, root, longHop);
        edge(analysis, shortHop, shared);
        edge(analysis, longHop, extraHop);
        edge(analysis, extraHop, shared);
        edge(analysis, shared, root);
        edge(analysis, disconnected, disconnected);

        assertThat(graph.findMinimumDepths(analysis))
                .extracting(ComponentDepth::componentId, ComponentDepth::depth)
                .containsExactly(
                        tuple(root, 0), tuple(shortHop, 1), tuple(longHop, 1),
                        tuple(extraHop, 2), tuple(shared, 2), tuple(disconnected, null));
    }

    @Test
    void ignoresEdgesAndComponentsFromOtherAnalyses() {
        long local = analysis();
        long foreign = analysis();
        long root = node(local, "root", true);
        long localOnly = node(local, "local-only", false);
        long foreignRoot = node(foreign, "root", true);
        long foreignChild = node(foreign, "foreign-child", false);
        edge(foreign, foreignRoot, foreignChild);
        edge(local, root, foreignChild);
        edge(local, foreignChild, localOnly);
        edge(foreign, root, localOnly);

        assertThat(graph.findMinimumDepths(local))
                .containsExactly(new ComponentDepth(root, "root", 0),
                        new ComponentDepth(localOnly, "local-only", null));
        assertThat(graph.findMinimumDepths(foreign))
                .containsExactly(new ComponentDepth(foreignRoot, "root", 0),
                        new ComponentDepth(foreignChild, "foreign-child", 1));
    }

    @Test
    void traversesPastTheFormerDepthLimit() {
        long analysis = analysis();
        long previous = node(analysis, "root", true);
        for (int depth = 1; depth <= 17; depth++) {
            long next = node(analysis, "library-" + depth, false);
            edge(analysis, previous, next);
            previous = next;
        }

        assertThat(graph.findMinimumDepths(analysis)).hasSize(18)
                .last().isEqualTo(new ComponentDepth(previous, "library-17", 17));
        assertThat(graph.findMinimumDepths(Long.MAX_VALUE)).isEmpty();
    }

    private long project() {
        return jdbc.queryForObject("INSERT INTO proyecto (nombre) VALUES ('RF-03 test') RETURNING id", Long.class);
    }

    private long analysis() {
        return jdbc.queryForObject("INSERT INTO analisis (proyecto_id) VALUES (?) RETURNING id", Long.class, project());
    }

    private long node(long analysis, String ref, boolean root) {
        return jdbc.queryForObject("""
                INSERT INTO componente (analisis_id, bom_ref, artifact_id, es_raiz)
                VALUES (?, ?, ?, ?) RETURNING id
                """, Long.class, analysis, ref, ref, root);
    }

    private void edge(long analysis, long parent, long child) {
        jdbc.update("""
                INSERT INTO relacion_dependencia (analisis_id, padre_componente_id, hijo_componente_id)
                VALUES (?, ?, ?)
                """, analysis, parent, child);
    }
}
