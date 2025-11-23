package br.com.felipebrandao.menufacil.dto.recipe;

import lombok.Data;

import java.util.List;

@Data
public class RecipeListResponse {
    private List<RecipeSummaryResponse> recipes;
    private Pagination pagination;
}
