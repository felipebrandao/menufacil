package br.com.felipebrandao.menufacil.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecipeIngredient {

    @NotBlank
    private String ingredientId;

    @NotBlank
    private String unitUsedId;

    @NotNull
    private Double quantity;
}
