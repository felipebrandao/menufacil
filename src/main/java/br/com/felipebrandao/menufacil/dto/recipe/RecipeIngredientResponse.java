package br.com.felipebrandao.menufacil.dto.recipe;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RecipeIngredientResponse {

    private String ingredientId;
    private String ingredientName;

    private String unitUsedId;
    private String unitUsedName;
    private String unitUsedAbbreviation;
    private Double quantity;
}
