package ar.edu.utn.frlp.securegraph.web.dto;

import java.util.List;

public record GraphResponseDto(
        Long analysisId,
        List<GraphNodeDto> nodes,
        List<GraphEdgeDto> edges
) {
    public record GraphNodeDto(
            String id,
            String label,
            String type,
            boolean vulnerable,
            String severity
    ) {
    }

    public record GraphEdgeDto(
            String source,
            String target,
            String relation
    ) {
    }
}

