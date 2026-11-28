package ar.edu.utn.frlp.securegraph.service;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@EnabledIfEnvironmentVariable(named = "SECUREGRAPH_TEST_POSTGRES_URL", matches = "jdbc:postgresql:.*")
class ApiContractPostgresTest {

    @Autowired private MockMvc mvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private ObjectMapper mapper;

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {
        DependencyGraphPostgresTest.configureDatabase(registry);
    }

    @Test
    void reportsAnAvailablePostgresConnection() throws Exception {
        mvc.perform(get("/api/health")).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void persistsNewUtcInstantsAndPreservesLegacyWallClockValues() throws Exception {
        long legacyId = jdbc.queryForObject("""
                INSERT INTO proyecto(nombre, created_at)
                VALUES ('Legacy timestamp', TIMESTAMP '2026-11-04 14:20:00')
                RETURNING id
                """, Long.class);

        var response = mvc.perform(post("/api/proyectos").contentType("application/json")
                        .content("{\"nombre\":\"Known instant\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse();
        var project = mapper.readTree(response.getContentAsByteArray());
        long newId = project.get("id").asLong();
        Instant apiInstant = Instant.parse(project.get("createdAt").asText());
        OffsetDateTime stored = jdbc.queryForObject("SELECT created_at_utc FROM proyecto WHERE id = ?",
                (row, index) -> row.getObject(1, OffsetDateTime.class), newId);

        assertThat(stored).isNotNull();
        assertThat(stored.toInstant()).isEqualTo(apiInstant);
        assertThat(jdbc.queryForObject("SELECT created_at FROM proyecto WHERE id = ?",
                LocalDateTime.class, legacyId)).isEqualTo(LocalDateTime.of(2026, 11, 4, 14, 20));
        OffsetDateTime legacyInstant = jdbc.queryForObject("SELECT created_at_utc FROM proyecto WHERE id = ?",
                (row, index) -> row.getObject(1, OffsetDateTime.class), legacyId);
        assertThat(legacyInstant).isNull();

        var listed = mvc.perform(get("/api/proyectos")).andExpect(status().isOk())
                .andReturn().getResponse();
        var projects = mapper.readTree(listed.getContentAsByteArray());
        assertThat(projects).anySatisfy(row -> {
            assertThat(row.get("id").asLong()).isEqualTo(newId);
            assertThat(row.get("createdAt").asText()).isEqualTo(apiInstant.toString());
        }).anySatisfy(row -> {
            assertThat(row.get("id").asLong()).isEqualTo(legacyId);
            assertThat(row.get("createdAt").asText()).isEqualTo("2026-11-04T14:20:00");
        });
    }
}
