package ar.edu.utn.frlp.securegraph.web.controller;

import ar.edu.utn.frlp.securegraph.domain.entity.Analisis;
import ar.edu.utn.frlp.securegraph.domain.entity.Proyecto;
import ar.edu.utn.frlp.securegraph.repository.AnalisisRepository;
import ar.edu.utn.frlp.securegraph.repository.ProyectoRepository;
import ar.edu.utn.frlp.securegraph.web.dto.ProyectoDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/proyectos")
public class ProyectoController {

    private final ProyectoRepository proyectoRepository;
    private final AnalisisRepository analisisRepository;

    public ProyectoController(ProyectoRepository proyectoRepository, AnalisisRepository analisisRepository) {
        this.proyectoRepository = proyectoRepository;
        this.analisisRepository = analisisRepository;
    }

    @GetMapping
    public ResponseEntity<List<ProyectoDto>> listarProyectos() {
        List<ProyectoDto> proyectos = proyectoRepository.findAll().stream()
                .map(proyecto -> {
                    List<Analisis> historial = analisisRepository.findByProyectoIdOrderByFechaAnalisisDesc(proyecto.getId());
                    Long ultimoAnalisisId = historial.isEmpty() ? null : historial.getFirst().getId();
                    return new ProyectoDto(
                            proyecto.getId(),
                            proyecto.getNombre(),
                            proyecto.getDescripcion(),
                            proyecto.getCreatedAt(),
                            ultimoAnalisisId,
                            historial.size()
                    );
                })
                .toList();
        return ResponseEntity.ok(proyectos);
    }

    @PostMapping
    public ResponseEntity<ProyectoDto> crearProyecto(@Valid @RequestBody ProyectoDto.CreateProyectoRequest request) {
        Proyecto proyecto = new Proyecto();
        proyecto.setNombre(request.nombre());
        proyecto.setDescripcion(request.descripcion());
        Proyecto guardado = proyectoRepository.save(proyecto);

        ProyectoDto dto = new ProyectoDto(
                guardado.getId(),
                guardado.getNombre(),
                guardado.getDescripcion(),
                guardado.getCreatedAt(),
                null,
                0
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PostMapping(value = "/{id}/analisis", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> subirSbomAnalisis(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "";
        if (file.isEmpty() || !filename.toLowerCase().endsWith(".json")) {
            return ResponseEntity.badRequest().body(Map.of(
                    "timestamp", Instant.now().toString(),
                    "status", 400,
                    "error", "Bad Request",
                    "message", "El archivo proporcionado no cumple con el esquema CycloneDX JSON válido.",
                    "path", "/api/proyectos/" + id + "/analisis"
            ));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "projectId", id,
                "analysisId", 42,
                "filename", filename,
                "status", "COMPLETED"
        ));
    }
}
