package br.com.felipebrandao.menufacil.dto.shopping;

import lombok.Data;

@Data
public class ShoppingListRecipeResponse {

    private String recipeId;
    private String recipeName;
    private Integer occurrences;
}

