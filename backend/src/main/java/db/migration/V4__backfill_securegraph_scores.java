package db.migration;

import ar.edu.utn.frlp.securegraph.service.SecureGraphScoreEngine;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.SQLException;

public class V4__backfill_securegraph_scores extends BaseJavaMigration {

    private static final int BATCH_SIZE = 500;

    @Override
    public void migrate(Context context) throws SQLException {
        var engine = new SecureGraphScoreEngine();
        try (var select = context.getConnection().prepareStatement("""
                SELECT h.id, h.severidad_cvss, h.nivel_severidad, h.profundidad, c.es_directa, c.scope
                FROM hallazgo_vulnerabilidad h
                JOIN componente c ON c.id = h.componente_id AND c.analisis_id = h.analisis_id
                WHERE h.razones_prioridad IS NULL
                ORDER BY h.id
                LIMIT ?
                """);
             var update = context.getConnection().prepareStatement("""
                UPDATE hallazgo_vulnerabilidad
                SET score_prioridad = ?, razones_prioridad = ?
                WHERE id = ?
                """)) {
            select.setInt(1, BATCH_SIZE);
            int rows;
            do {
                rows = 0;
                try (var findings = select.executeQuery()) {
                    while (findings.next()) {
                        var assessment = engine.assess(findings.getBigDecimal("severidad_cvss"),
                                findings.getString("nivel_severidad"), findings.getObject("profundidad", Integer.class),
                                findings.getBoolean("es_directa"), findings.getString("scope"));
                        update.setBigDecimal(1, assessment.score());
                        update.setString(2, assessment.reasonsText());
                        update.setLong(3, findings.getLong("id"));
                        update.addBatch();
                        rows++;
                    }
                }
                if (rows > 0) {
                    update.executeBatch();
                }
            } while (rows == BATCH_SIZE);
        }
        try (var statement = context.getConnection().createStatement()) {
            statement.execute("""
                    ALTER TABLE hallazgo_vulnerabilidad ADD CONSTRAINT chk_hallazgo_score_prioridad
                    CHECK (score_prioridad BETWEEN 0 AND 100)
                    """);
        }
    }
}
