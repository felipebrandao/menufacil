package br.com.felipebrandao.menufacil.dto.recipe;


import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class RecipeResponse {

    private String id;
    private String name;

    private RecipeCategoryResponse category;

    private List<RecipeIngredientResponse> ingredients;

    private List<String> instructions;

    private String mainImage;

    private List<String> gallery;

    private Integer totalTime;

    private Boolean highlighted;

    private Instant createdAt;

    private Instant updatedAt;
}
