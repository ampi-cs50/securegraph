package ar.edu.utn.frlp.securegraph.web.exception;

import ar.edu.utn.frlp.securegraph.service.AnalisisService.AnalysisNotFoundException;
import ar.edu.utn.frlp.securegraph.service.AnalisisService.InvalidAnalysisQueryException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Objects;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ValidationError handleValidation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        String message = exception.getBindingResult().getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .filter(Objects::nonNull)
                .sorted()
                .collect(Collectors.joining("; "));
        if (message.isBlank()) {
            message = "La solicitud contiene datos inválidos";
        }

        return error(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(InvalidAnalysisQueryException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ValidationError handleInvalidAnalysisQuery(InvalidAnalysisQueryException exception,
                                                       HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, exception.getMessage(), request);
    }

    @ExceptionHandler(AnalysisNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ValidationError handleAnalysisNotFound(HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, "El análisis no existe.", request);
    }

    private static ValidationError error(HttpStatus status, String message, HttpServletRequest request) {
        return new ValidationError(Instant.now().toString(), status.value(), status.getReasonPhrase(),
                message, request.getRequestURI());
    }

    public record ValidationError(String timestamp, int status, String error, String message, String path) {
    }
}
