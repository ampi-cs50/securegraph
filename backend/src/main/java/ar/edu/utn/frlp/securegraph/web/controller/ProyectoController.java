package ar.edu.utn.frlp.securegraph.web.controller;

import ar.edu.utn.frlp.securegraph.domain.entity.Analisis;
import ar.edu.utn.frlp.securegraph.domain.entity.Proyecto;
import ar.edu.utn.frlp.securegraph.repository.AnalisisRepository;
import ar.edu.utn.frlp.securegraph.repository.ProyectoRepository;
import ar.edu.utn.frlp.securegraph.web.dto.ProyectoDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

}
