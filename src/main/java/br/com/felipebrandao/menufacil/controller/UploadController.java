package br.com.felipebrandao.menufacil.controller;

import br.com.felipebrandao.menufacil.dto.upload.UploadedImageResponse;
import br.com.felipebrandao.menufacil.service.ImageStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/uploads")
@RequiredArgsConstructor
public class UploadController {

    private final ImageStorageService imageStorageService;

    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadedImageResponse> uploadImage(
            @RequestPart("file") MultipartFile file,
            @RequestParam(required = false) String context
    ) {
        UploadedImageResponse response = imageStorageService.upload(file, context);
        return ResponseEntity.status(201).body(response);
    }

    @PostMapping(value = "/images/batch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<UploadedImageResponse>> uploadImages(
            @RequestPart("files") List<MultipartFile> files,
            @RequestParam(required = false) String context
    ) {
        if (files == null || files.isEmpty()) {
            throw new IllegalArgumentException("Pelo menos um arquivo deve ser enviado");
        }

        List<UploadedImageResponse> responses = files.stream()
                .map(file -> imageStorageService.upload(file, context))
                .toList();

        return ResponseEntity.status(201).body(responses);
    }

    @DeleteMapping("/images")
    public ResponseEntity<Void> deleteImage(@RequestParam String publicId) {
        imageStorageService.delete(publicId);
        return ResponseEntity.noContent().build();
    }
}

