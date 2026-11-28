package ar.edu.utn.frlp.securegraph.service;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@EnabledIfEnvironmentVariable(named = "SECUREGRAPH_TEST_POSTGRES_URL", matches = "jdbc:postgresql:.*")
class SecureGraphScoreMigrationPostgresTest {

    @Test
    void backfillsExistingRf04FindingsWithoutChangingTheOriginalCvss() {
        String schema = "rf05_" + UUID.randomUUID().toString().replace("-", "");
        var source = new DriverManagerDataSource(
                System.getenv("SECUREGRAPH_TEST_POSTGRES_URL"),
                System.getenv().getOrDefault("SECUREGRAPH_TEST_POSTGRES_USER", "securegraph_user"),
                System.getenv().getOrDefault("SECUREGRAPH_TEST_POSTGRES_PASSWORD", "securegraph_pass"));
        var fixture = new JdbcTemplate(source);
        fixture.execute("CREATE SCHEMA " + schema);
        source.setSchema(schema);

        try {
            Flyway.configure().dataSource(source).schemas(schema).target("3").load().migrate();
            fixture.update("INSERT INTO proyecto (id, nombre) VALUES (1, 'RF-05 migration')");
            fixture.update("INSERT INTO analisis (id, proyecto_id) VALUES (1, 1)");
            fixture.update("""
                    INSERT INTO componente (id, analisis_id, bom_ref, artifact_id, scope, es_directa)
                    VALUES (1, 1, 'library', 'library', 'optional', FALSE)
                    """);
            fixture.update("""
                    INSERT INTO hallazgo_vulnerabilidad (analisis_id, componente_id, osv_id,
                                                        nivel_severidad, profundidad)
                    VALUES (1, 1, 'OSV-RF05', 'HIGH', 2)
                    """);

            var migration = Flyway.configure().dataSource(source).schemas(schema).target("4").load().migrate();

            assertThat(migration.migrationsExecuted).isEqualTo(1);
            assertThat(fixture.queryForMap("""
                    SELECT severidad_cvss, score_prioridad, razones_prioridad
                    FROM hallazgo_vulnerabilidad WHERE osv_id = 'OSV-RF05'
                    """)).containsEntry("severidad_cvss", null)
                    .containsEntry("score_prioridad", new BigDecimal("64.0"))
                    .satisfies(row -> assertThat(((String) row.get("razones_prioridad")).lines())
                            .hasSize(4).first().asString().contains("CVSS no informado", "Nivel HIGH"));
        } finally {
            source.setSchema(null);
            fixture.execute("DROP SCHEMA " + schema + " CASCADE");
        }
    }
}
