package br.com.felipebrandao.menufacil.model;

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

    @NotNull
    Ingredient ingredient;

    @NotNull
    UnitType unitUsed;

    @NotNull
    Double quantity;

    UnitType defaultUnit;

    Double quantityInDefaultUnit;
}
