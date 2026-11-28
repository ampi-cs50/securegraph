package ar.edu.utn.frlp.securegraph.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class SecureGraphScoreEngineTest {

    private final SecureGraphScoreEngine engine = new SecureGraphScoreEngine();

    @ParameterizedTest
    @CsvSource({
            "10, 1, true, required, 100.0",
            "0, 1, true, required, 0.0",
            "7.5, 3, false, required, 60.0",
            "5.9, 2, false, required, 48.7",
            "10, 2, false, optional, 80.0",
            "10, 2, false, excluded, 77.5",
            "10, 2, false, required, 82.5",
            "10, 2, true, required, 92.5",
            "10, 2147483647, false, required, 75.0",
            "10, 0, false, required, 75.0",
            "10, -1, false, required, 75.0"
    })
    void weightsEachInputAndRespectsTheRange(String cvss, int depth, boolean direct, String scope, String expected) {
        var assessment = engine.assess(new BigDecimal(cvss), "UNKNOWN", depth, direct, scope);

        assertThat(assessment.score()).isEqualByComparingTo(expected)
                .isBetween(BigDecimal.ZERO, BigDecimal.valueOf(100));
    }

    @ParameterizedTest
    @CsvSource({"CRITICAL, 76.0", "HIGH, 64.0", "MEDIUM, 44.0", "LOW, 16.0", "NONE, 0.0", "other, 28.0"})
    void usesDocumentedSeverityFactorsWhenCvssIsMissing(String severity, String expected) {
        var assessment = engine.assess(null, severity, 2, false, "optional");

        assertThat(assessment.score()).isEqualByComparingTo(expected);
        assertThat(assessment.reasons().getFirst()).contains("CVSS no informado", "asignado por SecureGraph");
    }

    @Test
    void explainsUnreachableComponentsAndProducesIdenticalResultsForTheSameInput() {
        var first = engine.assess(null, null, null, false, null);
        var second = engine.assess(null, null, null, false, null);

        assertThat(first).isEqualTo(second);
        assertThat(first.score()).isEqualByComparingTo("25.4");
        assertThat(first.reasons()).satisfiesExactly(
                reason -> assertThat(reason).contains("CVSS no informado", "UNKNOWN", "0.35"),
                reason -> assertThat(reason).contains("Sin profundidad positiva conocida"),
                reason -> assertThat(reason).contains("Sin relación directa"),
                reason -> assertThat(reason).contains("Alcance ausente o no reconocido", "2.5"));
        assertThat(first.reasonsText().lines().toList()).isEqualTo(first.reasons());
    }

    @Test
    void marksAnInvalidCvssAsUnavailableWithoutClampingItToAValidCvss() {
        var assessment = engine.assess(new BigDecimal("11"), " high ", 2, false, " OPTIONAL ");

        assertThat(assessment.score()).isEqualByComparingTo("64.0");
        assertThat(assessment.reasons().getFirst()).contains("CVSS fuera de 0–10", "Nivel HIGH");
    }
}
