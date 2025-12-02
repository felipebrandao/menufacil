package br.com.felipebrandao.menufacil.dto.ingredient;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class IngredientRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String category;

    @NotBlank
    private String defaultUnit;

    private List<ConversionRequest> conversions;
}
