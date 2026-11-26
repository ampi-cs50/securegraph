package ar.edu.utn.frlp.securegraph.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

@Component
public class OsvClient {

    private final RestClient client;
    private final URI endpoint;

    @Autowired
    public OsvClient(RestClient.Builder builder,
                     @Value("${securegraph.osv.base-url:https://api.osv.dev/v1}") String baseUrl,
                     @Value("${securegraph.osv.connect-timeout-seconds:3}") int connectTimeout,
                     @Value("${securegraph.osv.timeout-seconds:10}") int readTimeout) {
        this(builder.requestFactory(requestFactory(connectTimeout, readTimeout)).build(), baseUrl);
    }

    OsvClient(RestClient client, String baseUrl) {
        this.client = client;
        this.endpoint = URI.create(baseUrl.replaceFirst("/+$", "") + "/query");
    }

    private static SimpleClientHttpRequestFactory requestFactory(int connectTimeout, int readTimeout) {
        if (connectTimeout <= 0 || readTimeout <= 0) {
            throw new IllegalArgumentException("OSV timeouts must be positive");
        }
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Math.multiplyExact(connectTimeout, 1000));
        factory.setReadTimeout(Math.multiplyExact(readTimeout, 1000));
        return factory;
    }

    public List<Advisory> query(MavenPackage maven) {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("package", Map.of("ecosystem", "Maven", "name", maven.name()));
        request.put("version", maven.version());
        List<Advisory> advisories = new ArrayList<>();
        Set<String> visitedTokens = new HashSet<>();
        String token;
        do {
            JsonNode page = requestPage(request);
            JsonNode vulnerabilities = page.path("vulns");
            if (!vulnerabilities.isMissingNode() && !vulnerabilities.isArray()) {
                throw new OsvException(false);
            }
            vulnerabilities.forEach(entry -> advisories.add(readAdvisory(entry)));
            JsonNode next = page.path("next_page_token");
            if (!next.isMissingNode() && !next.isTextual()) {
                throw new OsvException(false);
            }
            token = next.asText("");
            if (!token.isEmpty()) {
                if (!visitedTokens.add(token)) {
                    throw new OsvException(false);
                }
                request.put("page_token", token);
            }
        } while (!token.isEmpty());
        return advisories;
    }

    private JsonNode requestPage(Map<String, Object> request) {
        try {
            JsonNode page = client.post().uri(endpoint)
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(request).retrieve().body(JsonNode.class);
            if (page == null || !page.isObject()) {
                throw new OsvException(false);
            }
            return page;
        } catch (ResourceAccessException exception) {
            throw new OsvException(true, exception);
        } catch (RestClientResponseException exception) {
            int status = exception.getStatusCode().value();
            throw new OsvException(status == 429 || status == 503 || status == 504, exception);
        } catch (RestClientException exception) {
            throw new OsvException(false, exception);
        }
    }

    private static Advisory readAdvisory(JsonNode entry) {
        String id = entry.path("id").asText("");
        if (!entry.path("id").isTextual() || id.isBlank() || id.length() > 100
                || id.chars().anyMatch(Character::isWhitespace)) {
            throw new OsvException(false);
        }
        Set<String> identifiers = new TreeSet<>();
        identifiers.add(id);
        entry.path("aliases").forEach(alias -> {
            if (alias.isTextual() && !alias.asText().isBlank()
                    && alias.asText().chars().noneMatch(Character::isWhitespace)) {
                identifiers.add(alias.asText());
            }
        });

        BigDecimal cvss = null;
        for (JsonNode rating : entry.path("severity")) {
            BigDecimal candidate = CvssScore.parse(rating.path("type").textValue(), rating.path("score").asText(null));
            if (candidate != null && (cvss == null || candidate.compareTo(cvss) > 0)) {
                cvss = candidate;
            }
        }
        String severity = cvss == null
                ? normalizeSeverity(entry.path("database_specific").path("severity").asText(""))
                : severityFromScore(cvss);
        return new Advisory(id, identifiers, cvss, severity);
    }

    private static String normalizeSeverity(String severity) {
        return switch (severity.toUpperCase(Locale.ROOT)) {
            case "CRITICAL" -> "CRITICAL";
            case "HIGH", "IMPORTANT" -> "HIGH";
            case "MEDIUM", "MODERATE" -> "MEDIUM";
            case "LOW" -> "LOW";
            case "NONE" -> "NONE";
            default -> "UNKNOWN";
        };
    }

    private static String severityFromScore(BigDecimal cvss) {
        double value = cvss.doubleValue();
        if (value >= 9) return "CRITICAL";
        if (value >= 7) return "HIGH";
        if (value >= 4) return "MEDIUM";
        return value > 0 ? "LOW" : "NONE";
    }

    public record MavenPackage(String name, String version) {
    }

    public record Advisory(String id, Set<String> identifiers, BigDecimal cvss, String severity) {
    }

    public static class OsvException extends RuntimeException {
        private final boolean unavailable;

        OsvException(boolean unavailable) {
            this(unavailable, null);
        }

        OsvException(boolean unavailable, Throwable cause) {
            super("OSV query failed", cause);
            this.unavailable = unavailable;
        }

        public boolean isUnavailable() {
            return unavailable;
        }
    }
}
