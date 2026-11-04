package ar.edu.utn.frlp.securegraph.web.dto;

import java.time.Instant;

public record HealthResponseDto(
        String status,
        String service,
        String database,
        Instant timestamp
) {
}

