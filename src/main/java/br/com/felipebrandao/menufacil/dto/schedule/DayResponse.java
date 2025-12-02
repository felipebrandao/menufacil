package br.com.felipebrandao.menufacil.dto.schedule;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class DayResponse {
    private LocalDate date;
    private List<ScheduledRecipeResponse> recipes;
    private Boolean hasRecipes;
    private Integer recipesCount;
}
