package br.com.felipebrandao.menufacil.service.impl;

import br.com.felipebrandao.menufacil.config.CloudinaryProperties;
import br.com.felipebrandao.menufacil.dto.upload.UploadedImageResponse;
import br.com.felipebrandao.menufacil.expection.ImageDeleteException;
import br.com.felipebrandao.menufacil.expection.ImageUploadException;
import br.com.felipebrandao.menufacil.expection.InvalidImageException;
import br.com.felipebrandao.menufacil.service.ImageStorageService;
import com.cloudinary.Cloudinary;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@ConditionalOnBean(Cloudinary.class)
public class CloudinaryImageStorageService implements ImageStorageService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private final Cloudinary cloudinary;
    private final CloudinaryProperties cloudinaryProperties;

    @Override
    public UploadedImageResponse upload(MultipartFile file, String context) {
        validateFile(file);

        Map<String, Object> options = new java.util.HashMap<>();
        options.put("resource_type", "image");
        options.put("folder", cloudinaryProperties.folder());
        if (context != null && !context.isBlank()) {
            options.put("tags", context);
        }

        try {
            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), options);
            return UploadedImageResponse.builder()
                    .secureUrl(asString(result.get("secure_url")))
                    .publicId(asString(result.get("public_id")))
                    .width(asInteger(result.get("width")))
                    .height(asInteger(result.get("height")))
                    .bytes(asLong(result.get("bytes")))
                    .format(asString(result.get("format")))
                    .build();
        } catch (IOException ex) {
            throw new ImageUploadException("Falha ao enviar imagem para o Cloudinary", ex);
        }
    }

    @Override
    public void delete(String publicId) {
        if (publicId == null || publicId.isBlank()) {
            throw new IllegalArgumentException("publicId é obrigatório");
        }

        try {
            cloudinary.uploader().destroy(publicId, Map.of("resource_type", "image"));
        } catch (IOException ex) {
            throw new ImageDeleteException("Falha ao remover imagem no Cloudinary", ex);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidImageException("Arquivo de imagem é obrigatório");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new InvalidImageException("Formato de imagem não suportado. Use JPEG, PNG ou WEBP");
        }
    }

    private String asString(Object value) {
        return value != null ? String.valueOf(value) : null;
    }

    private Integer asInteger(Object value) {
        return value instanceof Number number ? number.intValue() : null;
    }

    private Long asLong(Object value) {
        return value instanceof Number number ? number.longValue() : null;
    }
}

