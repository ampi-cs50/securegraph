package ar.edu.utn.frlp.securegraph.web.controller;

import ar.edu.utn.frlp.securegraph.domain.entity.Analisis;
import ar.edu.utn.frlp.securegraph.domain.entity.Componente;
import ar.edu.utn.frlp.securegraph.domain.entity.Proyecto;
import ar.edu.utn.frlp.securegraph.domain.entity.RelacionDependencia;
import ar.edu.utn.frlp.securegraph.repository.AnalisisRepository;
import ar.edu.utn.frlp.securegraph.repository.ComponenteRepository;
import ar.edu.utn.frlp.securegraph.repository.ProyectoRepository;
import ar.edu.utn.frlp.securegraph.repository.RelacionDependenciaRepository;
import ar.edu.utn.frlp.securegraph.service.SbomImportService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SbomController.class)
@Import(SbomImportService.class)
class SbomControllerTest {

    private static final String URL = "/api/proyectos/7/analisis";
    private static final String SBOM = """
            {
              "bomFormat": "CycloneDX",
              "specVersion": "1.5",
              "metadata": {"component": {"type": "application", "name": "app", "bom-ref": "app"}},
              "components": [
                {"type": "library", "name": "direct", "bom-ref": "direct", "group": "example",
                 "version": "1.0", "purl": "pkg:maven/example/direct@1.0", "scope": "optional"},
                {"type": "library", "name": "transitive", "bom-ref": "transitive"}
              ],
              "dependencies": [
                {"ref": "app", "dependsOn": ["direct"]},
                {"ref": "direct", "dependsOn": ["transitive"]}
              ]
            }
            """;

    @Autowired private MockMvc mockMvc;
    @MockBean private ProyectoRepository projects;
    @MockBean private AnalisisRepository analyses;
    @MockBean private ComponenteRepository components;
    @MockBean private RelacionDependenciaRepository dependencies;
    @Captor private ArgumentCaptor<Iterable<Componente>> savedComponents;
    @Captor private ArgumentCaptor<Iterable<RelacionDependencia>> savedEdges;

    @Test
    void importsComponentsAndEdgesWithThePersistedAnalysisId() throws Exception {
        Proyecto project = new Proyecto();
        project.setId(7L);
        when(projects.findById(7L)).thenReturn(Optional.of(project));
        when(analyses.save(any())).thenAnswer(invocation -> {
            Analisis analysis = invocation.getArgument(0);
            analysis.setId(108L);
            return analysis;
        });

        mockMvc.perform(multipart(URL).file(file("bom.json", SBOM)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.projectId").value(7))
                .andExpect(jsonPath("$.analysisId").value(108))
                .andExpect(jsonPath("$.filename").value("bom.json"))
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        verify(components).saveAll(savedComponents.capture());
        assertThat(savedComponents.getValue()).satisfiesExactly(
                root -> {
                    assertThat(root.getBomRef()).isEqualTo("app");
                    assertThat(root.getAnalisis().getProyecto()).isSameAs(project);
                    assertThat(root.getAnalisis().getTotalComponentes()).isEqualTo(2);
                },
                direct -> {
                    assertThat(direct.getEsDirecta()).isTrue();
                    assertThat(direct.getGroupId()).isEqualTo("example");
                    assertThat(direct.getArtifactId()).isEqualTo("direct");
                    assertThat(direct.getVersion()).isEqualTo("1.0");
                    assertThat(direct.getPurl()).isEqualTo("pkg:maven/example/direct@1.0");
                    assertThat(direct.getScope()).isEqualTo("optional");
                },
                transitive -> {
                    assertThat(transitive.getEsDirecta()).isFalse();
                    assertThat(transitive.getScope()).isEqualTo("required");
                });

        verify(dependencies).saveAll(savedEdges.capture());
        assertThat(savedEdges.getValue()).satisfiesExactly(
                direct -> assertEdge(direct, "app", "direct", false),
                transitive -> assertEdge(transitive, "direct", "transitive", true));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidFiles")
    void rejectsInvalidInputWithoutWritingData(String reason, String filename, String body) throws Exception {
        mockMvc.perform(multipart(URL).file(file(filename, body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").isString())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(
                        "El archivo proporcionado no cumple con el esquema CycloneDX JSON válido."))
                .andExpect(jsonPath("$.path").value(URL));

        verifyNoInteractions(projects, analyses, components, dependencies);
    }

    private static Stream<Arguments> invalidFiles() {
        return Stream.of(
                Arguments.of("empty upload", "bom.json", ""),
                Arguments.of("wrong extension", "bom.xml", SBOM),
                Arguments.of("malformed JSON", "bom.json", "{"),
                Arguments.of("unrelated JSON", "bom.json", "{}"),
                Arguments.of("trailing JSON", "bom.json", SBOM + "{}"),
                Arguments.of("unknown target", "bom.json",
                        SBOM.replace("[\"transitive\"]", "[\"missing\"]")),
                Arguments.of("dependency cycle", "bom.json",
                        SBOM.replace("[\"transitive\"]", "[\"app\"]")));
    }

    @Test
    void rejectsMissingFile() throws Exception {
        mockMvc.perform(multipart(URL)).andExpect(status().isBadRequest());
        verifyNoInteractions(projects, analyses, components, dependencies);
    }

    @Test
    void rejectsImportForUnknownProject() throws Exception {
        mockMvc.perform(multipart(URL).file(file("bom.json", SBOM)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("El proyecto no existe."))
                .andExpect(jsonPath("$.path").value(URL));
        verify(projects).findById(7L);
        verifyNoInteractions(analyses, components, dependencies);
    }

    private static MockMultipartFile file(String filename, String contents) {
        return new MockMultipartFile("file", filename, "application/json", contents.getBytes(StandardCharsets.UTF_8));
    }

    private static void assertEdge(RelacionDependencia edge, String parent, String child, boolean transitive) {
        assertThat(edge.getAnalisis().getId()).isEqualTo(108L);
        assertThat(edge.getPadreComponente().getBomRef()).isEqualTo(parent);
        assertThat(edge.getHijoComponente().getBomRef()).isEqualTo(child);
        assertThat(edge.getTipoTransitiva()).isEqualTo(transitive);
    }
}
