package ar.edu.utn.frlp.securegraph.web.exception;

import ar.edu.utn.frlp.securegraph.service.AnalisisService.AnalysisNotFoundException;
import ar.edu.utn.frlp.securegraph.service.AnalisisService.InvalidAnalysisQueryException;
import ar.edu.utn.frlp.securegraph.service.OsvClient.OsvException;
import ar.edu.utn.frlp.securegraph.service.SbomImportService.InvalidSbomException;
import ar.edu.utn.frlp.securegraph.service.SbomImportService.ProjectNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.TypeMismatchException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;
import java.util.Objects;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException exception,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String message = exception.getBindingResult().getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .filter(Objects::nonNull)
                .sorted()
                .collect(Collectors.joining("; "));
        return handleExceptionInternal(exception, message.isBlank() ? "La solicitud contiene datos inválidos" : message,
                headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception exception, Object body,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String message = switch (exception) {
            case HttpMessageNotReadableException ignored -> "El cuerpo de la solicitud no contiene un JSON válido.";
            case TypeMismatchException ignored -> "La solicitud contiene un parámetro con un tipo inválido.";
            default -> body instanceof String validationMessage ? validationMessage : safeMessage(status);
        };
        return super.handleExceptionInternal(exception,
                error(status, message, ((ServletWebRequest) request).getRequest()), headers, status, request);
    }

    @ExceptionHandler(InvalidAnalysisQueryException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleInvalidAnalysisQuery(InvalidAnalysisQueryException exception, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, exception.getMessage(), request);
    }

    @ExceptionHandler(AnalysisNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleAnalysisNotFound(HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, "El análisis no existe.", request);
    }

    @ExceptionHandler(InvalidSbomException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleInvalidSbom(HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST,
                "El archivo proporcionado no cumple con el esquema CycloneDX JSON válido.", request);
    }

    @ExceptionHandler(ProjectNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleProjectNotFound(HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, "El proyecto no existe.", request);
    }

    @ExceptionHandler(OsvException.class)
    public ResponseEntity<ApiError> handleOsvError(OsvException exception, HttpServletRequest request) {
        HttpStatus status = exception.isUnavailable() ? HttpStatus.SERVICE_UNAVAILABLE : HttpStatus.BAD_GATEWAY;
        return ResponseEntity.status(status).body(error(status,
                "No se pudo completar la consulta de vulnerabilidades a OSV.", request));
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleUnexpectedError(Exception exception, HttpServletRequest request) {
        logger.error("Unhandled REST request failure", exception);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, safeMessage(HttpStatus.INTERNAL_SERVER_ERROR), request);
    }

    private static String safeMessage(HttpStatusCode status) {
        return status.is5xxServerError() ? "No se pudo completar la solicitud." : "La solicitud no se pudo procesar.";
    }

    private static ApiError error(HttpStatusCode status, String message, HttpServletRequest request) {
        HttpStatus standard = HttpStatus.resolve(status.value());
        return new ApiError(Instant.now().toString(), status.value(),
                standard == null ? "Error HTTP" : standard.getReasonPhrase(), message, request.getRequestURI());
    }

    public record ApiError(String timestamp, int status, String error, String message, String path) {
    }
}
