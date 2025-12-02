package br.com.felipebrandao.menufacil.service.impl;

import br.com.felipebrandao.menufacil.dto.recipe.RecipeSummaryResponse;
import br.com.felipebrandao.menufacil.dto.schedule.CreateScheduleRecipeRequest;
import br.com.felipebrandao.menufacil.dto.schedule.DayResponse;
import br.com.felipebrandao.menufacil.dto.schedule.ReorderRequest;
import br.com.felipebrandao.menufacil.dto.schedule.ScheduleListResponse;
import br.com.felipebrandao.menufacil.dto.schedule.ScheduledRecipeResponse;
import br.com.felipebrandao.menufacil.model.Recipe;
import br.com.felipebrandao.menufacil.model.ScheduleDay;
import br.com.felipebrandao.menufacil.model.ScheduledRecipe;
import br.com.felipebrandao.menufacil.repository.RecipeRepository;
import br.com.felipebrandao.menufacil.repository.ScheduleRepository;
import br.com.felipebrandao.menufacil.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl  implements ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final RecipeRepository recipeRepository;

    @Override
    public ScheduleListResponse list(String view, LocalDate start, Integer year, Integer month) {
        LocalDate s;
        LocalDate e;
        if ("weekly".equalsIgnoreCase(view)) {
            s = start;
            e = start.plusDays(6);
        } else { // monthly
            YearMonth ym = YearMonth.of(year, month);
            s = ym.atDay(1);
            e = ym.atEndOfMonth();
        }

        List<ScheduleDay> stored = scheduleRepository.findByDateRange(s, e);
        Map<LocalDate, ScheduleDay> storedMap = stored.stream()
                .collect(Collectors.toMap(ScheduleDay::getDate, d -> d));

        Set<String> recipeIds = stored.stream()
                .flatMap(d -> d.getRecipes().stream().map(ScheduledRecipe::getRecipeId))
                .collect(Collectors.toSet());
        List<Recipe> foundRecipes = recipeIds.isEmpty()
                ? Collections.emptyList()
                : StreamSupport.stream(recipeRepository.findAllById(recipeIds).spliterator(), false).toList();
        Map<String, Recipe> recipeMap = foundRecipes.stream()
                .collect(Collectors.toMap(Recipe::getId, r -> r));

        ScheduleListResponse resp = new ScheduleListResponse();
        resp.setView(view);
        resp.setStart(s);
        resp.setEnd(e);

        List<DayResponse> dayResponses = new ArrayList<>();
        for (LocalDate d = s; !d.isAfter(e); d = d.plusDays(1)) {
            ScheduleDay sd = storedMap.get(d);
            List<ScheduledRecipeResponse> srList = new ArrayList<>();
            if (sd != null && sd.getRecipes() != null) {
                srList = sd.getRecipes().stream()
                        .sorted(Comparator.comparingInt(ScheduledRecipe::getOrder))
                        .map(r -> toScheduledResponse(r, recipeMap.get(r.getRecipeId())))
                        .collect(Collectors.toList());
            }

            DayResponse dr = new DayResponse();
            dr.setDate(d);
            dr.setRecipes(srList);
            dr.setHasRecipes(!srList.isEmpty());
            dr.setRecipesCount(srList.size());
            dayResponses.add(dr);
        }

        resp.setDays(dayResponses);
        return resp;
    }

    @Override
    public DayResponse getByDate(LocalDate date) {
        ScheduleDay day = scheduleRepository.findByDate(date)
                .orElseGet(() -> {
                    ScheduleDay d = new ScheduleDay();
                    d.setDate(date);
                    return d;
                });

        Set<String> recipeIds = day.getRecipes().stream()
                .map(ScheduledRecipe::getRecipeId)
                .collect(Collectors.toSet());
        Map<String, Recipe> recipeMap = recipeIds.isEmpty()
                ? Collections.emptyMap()
                : StreamSupport.stream(recipeRepository.findAllById(recipeIds).spliterator(), false)
                .collect(Collectors.toMap(Recipe::getId, r -> r));

        DayResponse dr = new DayResponse();
        dr.setDate(day.getDate());
        List<ScheduledRecipeResponse> list = day.getRecipes().stream()
                .sorted(Comparator.comparingInt(ScheduledRecipe::getOrder))
                .map(r -> toScheduledResponse(r, recipeMap.get(r.getRecipeId())))
                .collect(Collectors.toList());
        dr.setRecipes(list);
        dr.setHasRecipes(!list.isEmpty());
        dr.setRecipesCount(list.size());
        return dr;
    }

    @Transactional
    @Override
    public ScheduledRecipeResponse addRecipe(LocalDate date, CreateScheduleRecipeRequest req) {
        ScheduleDay day = scheduleRepository.findByDate(date)
                .orElseGet(() -> {
                    ScheduleDay d = new ScheduleDay();
                    d.setDate(date);
                    d.setCreatedAt(Instant.now());
                    return d;
                });

        // valida existencia da receita
        Recipe recipe = recipeRepository.findById(req.getRecipeId())
                .orElseThrow(() -> new NoSuchElementException("Receita não encontrada"));

        int nextOrder = day.getRecipes().stream()
                .mapToInt(ScheduledRecipe::getOrder)
                .max()
                .orElse(0) + 1;

        ScheduledRecipe item = ScheduledRecipe.builder()
                .id(UUID.randomUUID().toString())
                .recipeId(req.getRecipeId())
                .order(nextOrder)
                .build();

        day.getRecipes().add(item);
        day.setUpdatedAt(Instant.now());
        scheduleRepository.save(day);

        return toScheduledResponse(item, recipe);
    }

    @Transactional
    @Override
    public void reorder(LocalDate date, ReorderRequest req) {
        ScheduleDay day = scheduleRepository.findByDate(date)
                .orElseThrow(() -> new NoSuchElementException("Dia não encontrado"));

        Map<String, ScheduledRecipe> map = day.getRecipes().stream()
                .collect(Collectors.toMap(ScheduledRecipe::getId, r -> r));

        int idx = 1;
        for (String id : req.getOrder()) {
            ScheduledRecipe r = map.get(id);
            if (r != null) {
                r.setOrder(idx++);
            }
        }
        day.setUpdatedAt(Instant.now());
        scheduleRepository.save(day);
    }

    @Transactional
    @Override
    public void deleteRecipe(LocalDate date, String scheduledId) {
        ScheduleDay day = scheduleRepository.findByDate(date)
                .orElseThrow(() -> new NoSuchElementException("Dia não encontrado"));

        boolean removed = day.getRecipes().removeIf(r -> r.getId().equals(scheduledId));
        if (!removed) throw new NoSuchElementException("Agendamento não encontrado");
        // reindex orders
        List<ScheduledRecipe> sorted = day.getRecipes().stream()
                .sorted(Comparator.comparingInt(ScheduledRecipe::getOrder))
                .collect(Collectors.toList());
        for (int i = 0; i < sorted.size(); i++) {
            sorted.get(i).setOrder(i + 1);
        }
        day.setUpdatedAt(Instant.now());
        scheduleRepository.save(day);
    }

    private ScheduledRecipeResponse toScheduledResponse(ScheduledRecipe r, Recipe recipe) {
        ScheduledRecipeResponse dto = new ScheduledRecipeResponse();
        dto.setId(r.getId());
        dto.setRecipeId(r.getRecipeId());
        dto.setOrder(r.getOrder());

        if (recipe != null) {
            RecipeSummaryResponse sum = new RecipeSummaryResponse();
            sum.setId(recipe.getId());
            sum.setName(recipe.getName());
            sum.setCategory(recipe.getCategory() != null ? recipe.getCategory().getName() : null);
            sum.setMainImage(recipe.getMainImage());
            sum.setRating(0.0);
            sum.setTotalTime(recipe.getTotalTime());
            sum.setHighlighted(recipe.getHighlighted());
            sum.setCreatedAt(recipe.getCreatedAt());
            dto.setRecipeSummary(sum);
        }

        return dto;
    }
}
