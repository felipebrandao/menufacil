package br.com.felipebrandao.menufacil.dto.recipe;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class UpdateRecipeRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String categoryId;

    @Size(min = 1)
    private List<RecipeIngredientRequest> ingredients;

    private List<String> instructions;

    private String mainImage;

    private String mainImagePublicId;

    private List<String> gallery;

    private List<String> galleryPublicIds;

    private Integer totalTime;

    private Boolean highlighted;

}
