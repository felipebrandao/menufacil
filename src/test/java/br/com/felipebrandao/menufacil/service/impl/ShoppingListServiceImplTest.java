package br.com.felipebrandao.menufacil.service.impl;

import br.com.felipebrandao.menufacil.model.Recipe;
import br.com.felipebrandao.menufacil.model.ScheduleDay;
import br.com.felipebrandao.menufacil.model.ScheduledRecipe;
import br.com.felipebrandao.menufacil.repository.IngredientRepository;
import br.com.felipebrandao.menufacil.repository.RecipeRepository;
import br.com.felipebrandao.menufacil.repository.ScheduleRepository;
import br.com.felipebrandao.menufacil.repository.UnitTypeRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ShoppingListServiceImplTest {

    @Test
    void generate_weekly_shouldIgnoreDaysBeforeToday_whenStartIsInPast() {
        ScheduleRepository scheduleRepository = mock(ScheduleRepository.class);
        RecipeRepository recipeRepository = mock(RecipeRepository.class);
        IngredientRepository ingredientRepository = mock(IngredientRepository.class);
        UnitTypeRepository unitTypeRepository = mock(UnitTypeRepository.class);

        Clock clock = Clock.fixed(Instant.parse("2026-01-14T10:00:00Z"), ZoneId.of("UTC"));

        ShoppingListServiceImpl service = new ShoppingListServiceImpl(scheduleRepository, recipeRepository, ingredientRepository, unitTypeRepository, clock);

        LocalDate start = LocalDate.of(2026, 1, 11);

        when(scheduleRepository.findByDateRange(any(), any())).thenReturn(Collections.emptyList());

        service.generate("weekly", start, null, null);

        ArgumentCaptor<LocalDate> startCaptor = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<LocalDate> endCaptor = ArgumentCaptor.forClass(LocalDate.class);
        verify(scheduleRepository).findByDateRange(startCaptor.capture(), endCaptor.capture());

        assertThat(startCaptor.getValue()).isEqualTo(LocalDate.of(2026, 1, 14));
        assertThat(endCaptor.getValue()).isEqualTo(LocalDate.of(2026, 1, 17));

        verify(recipeRepository, never()).findAllById(any());
    }

    @Test
    void generate_weekly_shouldReturnEmptyWithoutQuery_whenWholeRangeIsPast() {
        ScheduleRepository scheduleRepository = mock(ScheduleRepository.class);
        RecipeRepository recipeRepository = mock(RecipeRepository.class);
        IngredientRepository ingredientRepository = mock(IngredientRepository.class);
        UnitTypeRepository unitTypeRepository = mock(UnitTypeRepository.class);

        Clock clock = Clock.fixed(Instant.parse("2026-01-20T10:00:00Z"), ZoneId.of("UTC"));

        ShoppingListServiceImpl service = new ShoppingListServiceImpl(scheduleRepository, recipeRepository, ingredientRepository, unitTypeRepository, clock);

        LocalDate start = LocalDate.of(2026, 1, 11);

        var resp = service.generate("weekly", start, null, null);

        verify(scheduleRepository, never()).findByDateRange(any(), any());
        verify(recipeRepository, never()).findAllById(any());

        assertThat(resp.getRecipes()).isEmpty();
        assertThat(resp.getCategories()).isEmpty();
    }

    @Test
    void generate_weekly_shouldReturnRecipesWithOccurrences_onlyFromEffectiveStart() {
        ScheduleRepository scheduleRepository = mock(ScheduleRepository.class);
        RecipeRepository recipeRepository = mock(RecipeRepository.class);
        IngredientRepository ingredientRepository = mock(IngredientRepository.class);
        UnitTypeRepository unitTypeRepository = mock(UnitTypeRepository.class);

        Clock clock = Clock.fixed(Instant.parse("2026-01-14T10:00:00Z"), ZoneId.of("UTC"));

        ShoppingListServiceImpl service = new ShoppingListServiceImpl(scheduleRepository, recipeRepository, ingredientRepository, unitTypeRepository, clock);

        LocalDate start = LocalDate.of(2026, 1, 11);

        ScheduleDay d14 = new ScheduleDay();
        d14.setDate(LocalDate.of(2026, 1, 14));
        d14.setRecipes(List.of(sr("r1"), sr("r1"), sr("r2")));

        ScheduleDay d15 = new ScheduleDay();
        d15.setDate(LocalDate.of(2026, 1, 15));
        d15.setRecipes(List.of(sr("r2")));

        when(scheduleRepository.findByDateRange(any(), any())).thenReturn(List.of(d14, d15));

        Recipe r1 = new Recipe();
        r1.setId("r1");
        r1.setName("Frango Grelhado");

        Recipe r2 = new Recipe();
        r2.setId("r2");
        r2.setName("Salada de Frutas");

        when(recipeRepository.findAllById(any())).thenReturn(List.of(r1, r2));

        var resp = service.generate("weekly", start, null, null);

        assertThat(resp.getRecipes()).hasSize(2);

        var rr1 = resp.getRecipes().stream().filter(r -> "r1".equals(r.getRecipeId())).findFirst().orElseThrow();
        var rr2 = resp.getRecipes().stream().filter(r -> "r2".equals(r.getRecipeId())).findFirst().orElseThrow();

        assertThat(rr1.getOccurrences()).isEqualTo(2);
        assertThat(rr2.getOccurrences()).isEqualTo(2);
    }

    private static ScheduledRecipe sr(String recipeId) {
        ScheduledRecipe sr = new ScheduledRecipe();
        sr.setRecipeId(recipeId);
        return sr;
    }
}
