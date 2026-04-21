package br.com.felipebrandao.menufacil.service;

import br.com.felipebrandao.menufacil.dto.upload.UploadedImageResponse;
import org.springframework.web.multipart.MultipartFile;

public interface ImageStorageService {
    UploadedImageResponse upload(MultipartFile file, String context);

    void delete(String publicId);
}

