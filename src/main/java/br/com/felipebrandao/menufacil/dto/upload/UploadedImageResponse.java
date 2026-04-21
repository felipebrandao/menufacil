package br.com.felipebrandao.menufacil.dto.upload;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UploadedImageResponse {
    String secureUrl;
    String publicId;
    Integer width;
    Integer height;
    Long bytes;
    String format;
}

