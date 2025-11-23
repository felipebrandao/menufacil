package br.com.felipebrandao.menufacil.dto.recipe;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RecipeSummaryResponse {
    private String id;
    private String name;
    private String category;
    private String mainImage;
    private Double rating;
    private Integer totalTime;
    private Boolean highlighted;
    private LocalDateTime createdAt;
}
