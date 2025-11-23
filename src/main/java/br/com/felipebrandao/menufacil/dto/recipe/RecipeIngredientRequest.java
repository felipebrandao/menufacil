package br.com.felipebrandao.menufacil.dto.recipe;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RecipeIngredientRequest {

    @NotBlank
    private String ingredientId;

    @NotBlank
    private String unitUsedId;

    @NotNull
    private Double quantity;
}
