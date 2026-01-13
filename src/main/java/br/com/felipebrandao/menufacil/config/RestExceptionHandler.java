package br.com.felipebrandao.menufacil.config;

import br.com.felipebrandao.menufacil.dto.error.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.Optional;

@ControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArg(IllegalArgumentException ex, HttpServletRequest request) {
        ApiErrorResponse body = base("BAD_REQUEST", ex.getMessage(), HttpStatus.BAD_REQUEST, request);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(NoSuchElementException ex, HttpServletRequest request) {
        ApiErrorResponse body = base("NOT_FOUND", ex.getMessage(), HttpStatus.NOT_FOUND, request);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ApiErrorResponse> handleDup(DuplicateKeyException ex, HttpServletRequest request) {
        ApiErrorResponse body = base("CONFLICT", "Recurso duplicado", HttpStatus.CONFLICT, request);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        ApiErrorResponse body = ApiErrorResponse.builder()
                .code("VALIDATION_ERROR")
                .message("Campos inválidos")
                .status(HttpStatus.BAD_REQUEST.value())
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .fields(
                        ex.getBindingResult().getFieldErrors().stream()
                                .map(fe -> ApiErrorResponse.FieldError.builder()
                                        .field(fe.getField())
                                        .message(Optional.ofNullable(fe.getDefaultMessage()).orElse("inv\u00e1lido"))
                                        .build()
                                )
                                .toList()
                )
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    private ApiErrorResponse base(String code, String message, HttpStatus status, HttpServletRequest request) {
        return ApiErrorResponse.builder()
                .code(code)
                .message(message)
                .status(status.value())
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .build();
    }
}
