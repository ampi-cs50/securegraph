package ar.edu.utn.frlp.securegraph.web.dto;

import java.util.List;

public record FindingsResponseDto(
        Long analysisId,
        SummaryDto summary,
        List<FindingItemDto> findings
) {
    public record SummaryDto(
            int components,
            int vulnerabilities,
            int critical,
            int high,
            int medium,
            int low
    ) {
    }

    public record FindingItemDto(
            Long id,
            String vulnerabilityId,
            String cveId,
            String component,
            String version,
            double cvss,
            String severity,
            boolean direct,
            int depth,
            double secureGraphScore,
            List<String> reasons
    ) {
    }
}

