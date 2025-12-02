package br.com.felipebrandao.menufacil.dto.schedule;

import br.com.felipebrandao.menufacil.dto.recipe.RecipeSummaryResponse;
import lombok.Data;

@Data
public class ScheduledRecipeResponse {
    private String id;
    private String recipeId;
    private Integer order;
    private RecipeSummaryResponse recipeSummary;
}
