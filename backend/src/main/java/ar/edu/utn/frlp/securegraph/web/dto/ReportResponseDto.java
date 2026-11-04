package ar.edu.utn.frlp.securegraph.web.dto;

import ar.edu.utn.frlp.securegraph.web.dto.FindingsResponseDto.FindingItemDto;
import ar.edu.utn.frlp.securegraph.web.dto.FindingsResponseDto.SummaryDto;

import java.time.Instant;
import java.util.List;

public record ReportResponseDto(
        Long analysisId,
        String projectName,
        String sbomFormat,
        Instant generatedAt,
        String orderingCriterion,
        SummaryDto summary,
        List<FindingItemDto> findings
) {
}

