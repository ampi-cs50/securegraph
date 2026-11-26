package ar.edu.utn.frlp.securegraph.service;

import us.springett.cvss.Cvss;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

final class CvssScore {

    private CvssScore() {
    }

    static BigDecimal parse(String type, String score) {
        if (score == null || type == null || !type.matches("CVSS_V[234]")) {
            return null;
        }
        if (score.matches("(?:[0-9]|10)(?:\\.[0-9])?")) {
            BigDecimal value = new BigDecimal(score);
            return value.compareTo(BigDecimal.TEN) <= 0 ? value : null;
        }

        String vector = score;
        Set<String> required;
        switch (type) {
            case "CVSS_V2" -> {
                vector = score.replaceFirst("^CVSS:2\\.0/", "");
                required = Set.of("AV", "AC", "Au", "C", "I", "A");
            }
            case "CVSS_V3" -> {
                if (!score.matches("CVSS:3\\.[01]/.+")) {
                    return null;
                }
                required = Set.of("AV", "AC", "PR", "UI", "S", "C", "I", "A");
            }
            case "CVSS_V4" -> {
                if (!score.startsWith("CVSS:4.0/")) {
                    return null;
                }
                required = Set.of("AV", "AC", "AT", "PR", "UI", "VC", "VI", "VA", "SC", "SI", "SA");
            }
            default -> throw new IllegalArgumentException("Unsupported CVSS type");
        }
        if (!containsRequiredMetrics(vector, required)) {
            return null;
        }
        try {
            Cvss parsed = Cvss.fromVector(vector);
            if (parsed == null) {
                return null;
            }
            double baseScore = parsed.calculateScore().getBaseScore();
            return baseScore >= 0 && baseScore <= 10 ? BigDecimal.valueOf(baseScore) : null;
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private static boolean containsRequiredMetrics(String vector, Set<String> required) {
        Set<String> keys = new HashSet<>();
        for (String metric : vector.replaceFirst("^CVSS:[^/]+/", "").split("/", -1)) {
            String[] pair = metric.split(":", -1);
            if (pair.length != 2 || pair[1].isEmpty() || !keys.add(pair[0])) {
                return false;
            }
        }
        return keys.containsAll(required);
    }
}
