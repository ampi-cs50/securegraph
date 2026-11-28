package ar.edu.utn.frlp.securegraph.web.dto;

import ar.edu.utn.frlp.securegraph.domain.entity.Proyecto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.format.DateTimeFormatter;

public record ProyectoDto(
        Long id,
        String nombre,
        String descripcion,
        String createdAt,
        Long ultimoAnalisisId,
        Integer totalAnalisis
) {
    public static ProyectoDto from(Proyecto project, Long latestAnalysisId, int analysisCount) {
        var instant = project.getCreatedAtInstant();
        var legacy = project.getCreatedAt();
        String timestamp = instant != null ? instant.toString()
                : legacy == null ? null : legacy.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        return new ProyectoDto(project.getId(), project.getNombre(), project.getDescripcion(),
                timestamp, latestAnalysisId, analysisCount);
    }

    public record CreateProyectoRequest(
            @NotBlank(message = "El nombre del proyecto es obligatorio")
            @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
            String nombre,
            String descripcion
    ) {
    }
}

