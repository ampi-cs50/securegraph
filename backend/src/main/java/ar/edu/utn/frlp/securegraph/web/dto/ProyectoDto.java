package ar.edu.utn.frlp.securegraph.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record ProyectoDto(
        Long id,
        String nombre,
        String descripcion,
        LocalDateTime createdAt,
        Long ultimoAnalisisId,
        Integer totalAnalisis
) {
    public record CreateProyectoRequest(
            @NotBlank(message = "El nombre del proyecto es obligatorio")
            @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
            String nombre,
            String descripcion
    ) {
    }
}

