package ar.edu.utn.frlp.securegraph.web.controller;

import ar.edu.utn.frlp.securegraph.service.AnalisisService;
import ar.edu.utn.frlp.securegraph.service.AnalisisService.InvalidAnalysisQueryException;
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
    public ResponseEntity<SummaryDto> obtenerResumen(@PathVariable String id) {
        return ResponseEntity.ok(analisisService.obtenerResumen(parseId(id)));
    }

    @GetMapping("/{id}/hallazgos")
    public ResponseEntity<FindingsResponseDto> obtenerHallazgos(
            @PathVariable String id,
            @RequestParam(defaultValue = "score") String orden,
            @RequestParam(required = false) String severidad) {
        return ResponseEntity.ok(analisisService.obtenerHallazgos(parseId(id), orden, severidad));
    }

    @GetMapping("/{id}/grafo")
    public ResponseEntity<GraphResponseDto> obtenerGrafo(@PathVariable String id) {
        return ResponseEntity.ok(analisisService.obtenerGrafo(parseId(id)));
    }

    @GetMapping("/{id}/reporte")
    public ResponseEntity<ReportResponseDto> obtenerReporte(
            @PathVariable String id,
            @RequestParam(defaultValue = "score") String orden) {
        return ResponseEntity.ok(analisisService.obtenerReporte(parseId(id), orden));
    }

    private static Long parseId(String id) {
        try {
            return Long.valueOf(id);
        } catch (NumberFormatException exception) {
            throw new InvalidAnalysisQueryException("El identificador del análisis debe ser un entero positivo.");
        }
    }
}
