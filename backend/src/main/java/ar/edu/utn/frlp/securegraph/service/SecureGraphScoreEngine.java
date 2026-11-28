package ar.edu.utn.frlp.securegraph.service;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;

@Component
public class SecureGraphScoreEngine {

    private static final BigDecimal BASE_POINTS = BigDecimal.valueOf(70);
    private static final BigDecimal DEPTH_POINTS = BigDecimal.valueOf(15);
    private static final BigDecimal DIRECT_POINTS = BigDecimal.TEN;

    public Assessment assess(BigDecimal cvss, String severity, Integer depth, boolean direct, String scope) {
        boolean validCvss = cvss != null && cvss.signum() >= 0 && cvss.compareTo(BigDecimal.TEN) <= 0;
        Severity fallback = Severity.from(severity);
        BigDecimal severityFactor = validCvss ? cvss.movePointLeft(1) : fallback.factor;
        String severityReason = validCvss
                ? "CVSS base " + decimal(cvss) + ". Factor de severidad " + decimal(severityFactor) + "."
                : (cvss == null ? "CVSS no informado" : "CVSS fuera de 0–10")
                    + ". Nivel " + fallback.name() + " con factor de severidad "
                    + decimal(severityFactor) + " asignado por SecureGraph.";

        boolean reachable = depth != null && depth > 0;
        String depthReason = reachable
                ? "Profundidad mínima " + depth + " desde la raíz. Aporta 15 / " + depth + " puntos antes de aplicar severidad."
                : "Sin profundidad positiva conocida desde la raíz. La proximidad no suma puntos.";
        String relationReason = relationReason(depth, direct);

        Scope exposure = Scope.from(scope);
        BigDecimal context = BASE_POINTS.add(exposure.points);
        if (direct) {
            context = context.add(DIRECT_POINTS);
        }
        // Keep the depth term exact until the final rounding.
        BigDecimal divisor = reachable ? BigDecimal.valueOf(depth) : BigDecimal.ONE;
        BigDecimal numerator = context.multiply(divisor).add(reachable ? DEPTH_POINTS : BigDecimal.ZERO);
        BigDecimal score = severityFactor.multiply(numerator).divide(divisor, 1, RoundingMode.HALF_UP);
        return new Assessment(score, List.of(severityReason, depthReason, relationReason,
                "Alcance " + exposure.label + ". Aporta " + decimal(exposure.points)
                        + " puntos antes de aplicar severidad."));
    }

    private static String relationReason(Integer depth, boolean direct) {
        if (direct) {
            return "Dependencia directa del proyecto. Aporta 10 puntos antes de aplicar severidad.";
        }
        if (depth != null && depth > 1) {
            return "Dependencia transitiva del proyecto. El carácter de la dependencia no suma puntos.";
        }
        return "Sin relación directa con el proyecto. El carácter de la dependencia no suma puntos.";
    }

    private static String decimal(BigDecimal value) {
        return value.stripTrailingZeros().toPlainString();
    }

    private enum Severity {
        CRITICAL("0.95"), HIGH("0.80"), MEDIUM("0.55"), LOW("0.20"), NONE("0"), UNKNOWN("0.35");

        private final BigDecimal factor;

        Severity(String factor) {
            this.factor = new BigDecimal(factor);
        }

        static Severity from(String severity) {
            if (severity == null) {
                return UNKNOWN;
            }
            return switch (severity.strip().toUpperCase(Locale.ROOT)) {
                case "CRITICAL" -> CRITICAL;
                case "HIGH" -> HIGH;
                case "MEDIUM" -> MEDIUM;
                case "LOW" -> LOW;
                case "NONE" -> NONE;
                default -> UNKNOWN;
            };
        }
    }

    private enum Scope {
        REQUIRED("required", "5"), OPTIONAL("optional", "2.5"), EXCLUDED("excluded", "0"),
        UNKNOWN("ausente o no reconocido", "2.5");

        private final String label;
        private final BigDecimal points;

        Scope(String label, String points) {
            this.label = label;
            this.points = new BigDecimal(points);
        }

        static Scope from(String scope) {
            if (scope == null) {
                return UNKNOWN;
            }
            return switch (scope.strip().toLowerCase(Locale.ROOT)) {
                case "required" -> REQUIRED;
                case "optional" -> OPTIONAL;
                case "excluded" -> EXCLUDED;
                default -> UNKNOWN;
            };
        }
    }

    public record Assessment(BigDecimal score, List<String> reasons) {
        public String reasonsText() {
            return String.join("\n", reasons);
        }
    }
}
