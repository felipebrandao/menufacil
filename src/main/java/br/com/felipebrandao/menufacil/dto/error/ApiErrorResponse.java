package br.com.felipebrandao.menufacil.dto.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.List;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiErrorResponse {
    String code;
    String message;
    Integer status;
    String path;
    Instant timestamp;
    List<FieldError> fields;

    @Value
    @Builder
    public static class FieldError {
        String field;
        String message;
    }
}

