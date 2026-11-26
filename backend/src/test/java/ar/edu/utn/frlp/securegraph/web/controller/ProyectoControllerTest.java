package ar.edu.utn.frlp.securegraph.web.controller;

import ar.edu.utn.frlp.securegraph.domain.entity.Analisis;
import ar.edu.utn.frlp.securegraph.domain.entity.Proyecto;
import ar.edu.utn.frlp.securegraph.repository.AnalisisRepository;
import ar.edu.utn.frlp.securegraph.repository.ProyectoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProyectoController.class)
class ProyectoControllerTest {

    private static final String PATH = "/api/proyectos";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProyectoRepository proyectoRepository;

    @MockBean
    private AnalisisRepository analisisRepository;

    @Test
    void shouldListProjectsWithAnalysisSummary() throws Exception {
        Proyecto proyecto = new Proyecto();
        proyecto.setId(1L);
        proyecto.setNombre("facturacion-core");
        proyecto.setDescripcion("Servicio de facturación");
        proyecto.setCreatedAt(LocalDateTime.of(2026, 11, 4, 14, 20));

        Analisis ultimo = new Analisis();
        ultimo.setId(42L);
        when(proyectoRepository.findAll()).thenReturn(List.of(proyecto));
        when(analisisRepository.findByProyectoIdOrderByFechaAnalisisDesc(1L)).thenReturn(List.of(ultimo));

        mockMvc.perform(get(PATH))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nombre").value("facturacion-core"))
                .andExpect(jsonPath("$[0].descripcion").value("Servicio de facturación"))
                .andExpect(jsonPath("$[0].createdAt").value("2026-11-04T14:20:00"))
                .andExpect(jsonPath("$[0].ultimoAnalisisId").value(42))
                .andExpect(jsonPath("$[0].totalAnalisis").value(1));
    }

    @Test
    void shouldCreateProject() throws Exception {
        when(proyectoRepository.save(any(Proyecto.class))).thenAnswer(invocation -> {
            Proyecto proyecto = invocation.getArgument(0);
            proyecto.setId(1L);
            proyecto.setCreatedAt(LocalDateTime.of(2026, 11, 4, 14, 20));
            return proyecto;
        });

        mockMvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"facturacion-core","descripcion":"Servicio de facturación"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("facturacion-core"))
                .andExpect(jsonPath("$.descripcion").value("Servicio de facturación"))
                .andExpect(jsonPath("$.createdAt").value("2026-11-04T14:20:00"))
                .andExpect(jsonPath("$.ultimoAnalisisId").hasJsonPath())
                .andExpect(jsonPath("$.ultimoAnalisisId").isEmpty())
                .andExpect(jsonPath("$.totalAnalisis").value(0));
    }

    @Test
    void shouldRejectEmptyProjectName() throws Exception {
        assertValidationError("", "El nombre del proyecto es obligatorio");
    }

    @Test
    void shouldRejectProjectNameLongerThan100Characters() throws Exception {
        assertValidationError("a".repeat(101), "El nombre no puede superar los 100 caracteres");
    }

    private void assertValidationError(String nombre, String message) throws Exception {
        mockMvc.perform(post(PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre":"%s"}
                                """.formatted(nombre)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$.timestamp").isString())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value(message))
                .andExpect(jsonPath("$.path").value(PATH))
                .andExpect(result -> Instant.parse(objectMapper.readTree(
                        result.getResponse().getContentAsByteArray()).get("timestamp").asText()));

        verifyNoInteractions(proyectoRepository, analisisRepository);
    }
}
