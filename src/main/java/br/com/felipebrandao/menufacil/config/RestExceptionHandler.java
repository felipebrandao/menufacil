package br.com.felipebrandao.menufacil.config;

import br.com.felipebrandao.menufacil.dto.error.ApiErrorResponse;
import br.com.felipebrandao.menufacil.expection.ImageDeleteException;
import br.com.felipebrandao.menufacil.expection.ImageUploadException;
import br.com.felipebrandao.menufacil.expection.InvalidImageException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@ControllerAdvice
public class RestExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(RestExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArg(IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("Bad request for {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(base("BAD_REQUEST", ex.getMessage(), HttpStatus.BAD_REQUEST, request));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(NoSuchElementException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(base("NOT_FOUND", ex.getMessage(), HttpStatus.NOT_FOUND, request));
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ApiErrorResponse> handleDup(DuplicateKeyException ignored, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(base("CONFLICT", "Recurso duplicado", HttpStatus.CONFLICT, request));
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
                                        .message(Optional.ofNullable(fe.getDefaultMessage()).orElse("inválido"))
                                        .build())
                                .toList()
                )
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        log.warn("Constraint violation for {} {}: {}", request.getMethod(), request.getRequestURI(), ex.getMessage(), ex);
        List<ApiErrorResponse.FieldError> fields = ex.getConstraintViolations().stream()
                .map(v -> ApiErrorResponse.FieldError.builder()
                        .field(String.valueOf(v.getPropertyPath()))
                        .message(Optional.ofNullable(v.getMessage()).orElse("inválido"))
                        .build())
                .toList();

        ApiErrorResponse body = ApiErrorResponse.builder()
                .code("VALIDATION_ERROR")
                .message("Parâmetros inválidos")
                .status(HttpStatus.BAD_REQUEST.value())
                .path(request.getRequestURI())
                .timestamp(Instant.now())
                .fields(fields)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleNotReadable(HttpMessageNotReadableException ignored, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(base("MALFORMED_REQUEST", "Corpo da requisição inválido", HttpStatus.BAD_REQUEST, request));
    }

    @ExceptionHandler(InvalidImageException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidImage(InvalidImageException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(base("INVALID_IMAGE", ex.getMessage(), HttpStatus.BAD_REQUEST, request));
    }

    @ExceptionHandler(ImageUploadException.class)
    public ResponseEntity<ApiErrorResponse> handleImageUpload(ImageUploadException ex, HttpServletRequest request) {
        log.error("Upload provider error for {} {}", request.getMethod(), request.getRequestURI(), ex);
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(base("IMAGE_UPLOAD_ERROR", "Falha no provedor de imagens", HttpStatus.BAD_GATEWAY, request));
    }

    @ExceptionHandler(ImageDeleteException.class)
    public ResponseEntity<ApiErrorResponse> handleImageDelete(ImageDeleteException ex, HttpServletRequest request) {
        log.error("Delete provider error for {} {}", request.getMethod(), request.getRequestURI(), ex);
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(base("IMAGE_DELETE_ERROR", "Falha no provedor de imagens", HttpStatus.BAD_GATEWAY, request));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiErrorResponse> handleMaxUploadSize(MaxUploadSizeExceededException ignored, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(base("UPLOAD_SIZE_EXCEEDED", "Arquivo excede o tamanho máximo permitido", HttpStatus.PAYLOAD_TOO_LARGE, request));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception for {} {}", request.getMethod(), request.getRequestURI(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(base("INTERNAL_ERROR", "Erro interno", HttpStatus.INTERNAL_SERVER_ERROR, request));
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
