package ar.edu.utn.frlp.securegraph.web.controller;

import ar.edu.utn.frlp.securegraph.service.AnalisisService;
import ar.edu.utn.frlp.securegraph.web.dto.FindingsResponseDto;
import ar.edu.utn.frlp.securegraph.web.dto.FindingsResponseDto.SummaryDto;
import ar.edu.utn.frlp.securegraph.web.dto.GraphResponseDto;
import ar.edu.utn.frlp.securegraph.web.dto.ReportResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analisis")
public class AnalisisController {

    private final AnalisisService analisisService;

    public AnalisisController(AnalisisService analisisService) {
        this.analisisService = analisisService;
    }

    @GetMapping("/{id}/resumen")
    public ResponseEntity<SummaryDto> obtenerResumen(@PathVariable Long id) {
        return ResponseEntity.ok(analisisService.obtenerResumen(id));
    }

    @GetMapping("/{id}/hallazgos")
    public ResponseEntity<FindingsResponseDto> obtenerHallazgos(
            @PathVariable Long id,
            @RequestParam(defaultValue = "score") String orden,
            @RequestParam(required = false) String severidad) {
        return ResponseEntity.ok(analisisService.obtenerHallazgos(id, orden, severidad));
    }

    @GetMapping("/{id}/grafo")
    public ResponseEntity<GraphResponseDto> obtenerGrafo(@PathVariable Long id) {
        return ResponseEntity.ok(analisisService.obtenerGrafo(id));
    }

    @GetMapping("/{id}/reporte")
    public ResponseEntity<ReportResponseDto> obtenerReporte(
            @PathVariable Long id,
            @RequestParam(defaultValue = "score") String orden) {
        return ResponseEntity.ok(analisisService.obtenerReporte(id, orden));
    }
}
