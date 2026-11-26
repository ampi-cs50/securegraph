package ar.edu.utn.frlp.securegraph.web.controller;

import ar.edu.utn.frlp.securegraph.service.SbomImportService;
import ar.edu.utn.frlp.securegraph.service.SbomImportService.ImportResult;
import ar.edu.utn.frlp.securegraph.service.SbomImportService.InvalidSbomException;
import ar.edu.utn.frlp.securegraph.service.SbomImportService.ProjectNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;

@RestController
@RequestMapping("/api/proyectos/{id}/analisis")
public class SbomController {

    private final SbomImportService importService;

    public SbomController(SbomImportService importService) {
        this.importService = importService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImportResult> importSbom(
            @PathVariable Long id, @RequestParam(value = "file", required = false) MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED).body(importService.importSbom(id, file));
    }

    @ExceptionHandler(InvalidSbomException.class)
    public ResponseEntity<ErrorResponse> invalidSbom(HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST,
                "El archivo proporcionado no cumple con el esquema CycloneDX JSON válido.",
                request);
    }

    @ExceptionHandler(ProjectNotFoundException.class)
    public ResponseEntity<ErrorResponse> projectNotFound(HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, "El proyecto no existe.", request);
    }

    private ResponseEntity<ErrorResponse> error(HttpStatus status, String message, HttpServletRequest request) {
        return ResponseEntity.status(status).body(new ErrorResponse(
                Instant.now().toString(), status.value(), status.getReasonPhrase(), message, request.getRequestURI()));
    }

    public record ErrorResponse(String timestamp, int status, String error, String message, String path) {
    }
}
