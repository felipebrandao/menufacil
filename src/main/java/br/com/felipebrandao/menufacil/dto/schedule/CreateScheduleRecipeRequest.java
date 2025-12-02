package br.com.felipebrandao.menufacil.dto.schedule;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateScheduleRecipeRequest {
    @NotBlank
    private String recipeId;
}
