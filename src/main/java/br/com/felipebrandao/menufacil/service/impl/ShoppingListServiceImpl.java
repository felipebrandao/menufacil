package br.com.felipebrandao.menufacil.service.impl;

import br.com.felipebrandao.menufacil.dto.shopping.ShoppingListCategoryResponse;
import br.com.felipebrandao.menufacil.dto.shopping.ShoppingListItemResponse;
import br.com.felipebrandao.menufacil.dto.shopping.ShoppingListRecipeResponse;
import br.com.felipebrandao.menufacil.dto.shopping.ShoppingListResponse;
import br.com.felipebrandao.menufacil.model.CategoryIngredient;
import br.com.felipebrandao.menufacil.model.Ingredient;
import br.com.felipebrandao.menufacil.model.Recipe;
import br.com.felipebrandao.menufacil.model.RecipeIngredient;
import br.com.felipebrandao.menufacil.model.ScheduleDay;
import br.com.felipebrandao.menufacil.model.ScheduledRecipe;
import br.com.felipebrandao.menufacil.model.UnitType;
import br.com.felipebrandao.menufacil.repository.IngredientRepository;
import br.com.felipebrandao.menufacil.repository.RecipeRepository;
import br.com.felipebrandao.menufacil.repository.ScheduleRepository;
import br.com.felipebrandao.menufacil.repository.UnitTypeRepository;
import br.com.felipebrandao.menufacil.service.ShoppingListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Primary
public class ShoppingListServiceImpl implements ShoppingListService {

    private final ScheduleRepository scheduleRepository;
    private final RecipeRepository recipeRepository;
    private final IngredientRepository ingredientRepository;
    private final UnitTypeRepository unitTypeRepository;
    private final Clock clock;

    @Autowired
    public ShoppingListServiceImpl(
            ScheduleRepository scheduleRepository,
            RecipeRepository recipeRepository,
            IngredientRepository ingredientRepository,
            UnitTypeRepository unitTypeRepository,
            Clock clock
    ) {
        this.scheduleRepository = scheduleRepository;
        this.recipeRepository = recipeRepository;
        this.ingredientRepository = ingredientRepository;
        this.unitTypeRepository = unitTypeRepository;
        this.clock = clock;
    }

    @Override
    public ShoppingListResponse generate(String view, LocalDate start, Integer year, Integer month) {
        DateRange requestedRange = resolveRange(view, start, year, month);

        LocalDate today = LocalDate.now(clock);
        LocalDate effectiveStart = requestedRange.start().isBefore(today) ? today : requestedRange.start();

        List<ScheduleDay> days = effectiveStart.isAfter(requestedRange.end())
                ? Collections.emptyList()
                : scheduleRepository.findByDateRange(effectiveStart, requestedRange.end());

        Map<String, Integer> recipeOccurrences = extractRecipeOccurrences(days != null ? days : Collections.emptyList());
        Set<String> recipeIds = recipeOccurrences.keySet();

        List<Recipe> recipes = recipeIds.isEmpty()
                ? Collections.emptyList()
                : recipeRepository.findAllById(recipeIds);

        if (recipes == null) recipes = Collections.emptyList();

        Aggregation aggregation = aggregateInDefaultUnit(recipes);
        List<ShoppingListCategoryResponse> categories = toCategoryResponses(aggregation);

        ShoppingListResponse resp = new ShoppingListResponse();
        resp.setView(view);
        resp.setStart(requestedRange.start());
        resp.setEnd(requestedRange.end());
        resp.setRecipes(toRecipeResponses(recipes, recipeOccurrences));
        resp.setCategories(categories);
        return resp;
    }

    private DateRange resolveRange(String view, LocalDate start, Integer year, Integer month) {
        if ("weekly".equalsIgnoreCase(view)) {
            if (start == null) {
                throw new IllegalArgumentException("Parâmetro 'start' é obrigatório para visão weekly");
            }
            return new DateRange(start, start.plusDays(6));
        }

        if (year == null || month == null) {
            throw new IllegalArgumentException("Parâmetros 'year' e 'month' são obrigatórios para visão monthly");
        }
        YearMonth ym = YearMonth.of(year, month);
        return new DateRange(ym.atDay(1), ym.atEndOfMonth());
    }

    private Map<String, Integer> extractRecipeOccurrences(List<ScheduleDay> days) {
        Map<String, Integer> occurrences = new HashMap<>();

        for (ScheduleDay day : days) {
            if (day == null || day.getRecipes() == null) continue;

            for (ScheduledRecipe sr : day.getRecipes()) {
                if (sr == null || sr.getRecipeId() == null) continue;
                occurrences.merge(sr.getRecipeId(), 1, Integer::sum);
            }
        }

        return occurrences;
    }

    private List<ShoppingListRecipeResponse> toRecipeResponses(List<Recipe> recipes, Map<String, Integer> occurrencesByRecipeId) {
        if (recipes == null || recipes.isEmpty()) return Collections.emptyList();

        return recipes.stream()
                .filter(Objects::nonNull)
                .map(r -> {
                    ShoppingListRecipeResponse rr = new ShoppingListRecipeResponse();
                    rr.setRecipeId(r.getId());
                    rr.setRecipeName(r.getName());
                    rr.setOccurrences(occurrencesByRecipeId.getOrDefault(r.getId(), 0));
                    return rr;
                })
                .sorted(Comparator
                        .comparing(ShoppingListRecipeResponse::getOccurrences, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(r -> r.getRecipeName() == null ? "" : r.getRecipeName(), String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private Aggregation aggregateInDefaultUnit(List<Recipe> recipes) {
        Map<String, CategoryIngredient> categoryById = new HashMap<>();
        Map<String, Ingredient> ingredientById = new HashMap<>();
        Map<String, UnitType> unitById = new HashMap<>();
        Map<Key, Double> totalsInDefaultUnit = new HashMap<>();

        if (recipes == null || recipes.isEmpty()) {
            return new Aggregation(categoryById, ingredientById, unitById, totalsInDefaultUnit);
        }

        Set<String> ingredientIds = recipes.stream()
                .filter(Objects::nonNull)
                .flatMap(r -> r.getIngredients() == null ? java.util.stream.Stream.empty() : r.getIngredients().stream())
                .map(RecipeIngredient::getIngredientId)
                .filter(id -> id != null && !id.isBlank())
                .collect(Collectors.toSet());

        if (!ingredientIds.isEmpty() && ingredientRepository != null) {
            for (Ingredient ing : ingredientRepository.findAllById(ingredientIds)) {
                if (ing == null || ing.getId() == null) continue;
                ingredientById.put(ing.getId(), ing);

                if (ing.getCategory() != null && ing.getCategory().getId() != null) {
                    categoryById.putIfAbsent(ing.getCategory().getId(), ing.getCategory());
                }
                if (ing.getDefaultUnit() != null && ing.getDefaultUnit().getId() != null) {
                    unitById.putIfAbsent(ing.getDefaultUnit().getId(), ing.getDefaultUnit());
                }
            }
        }

        Set<String> unitUsedIds = recipes.stream()
                .filter(Objects::nonNull)
                .flatMap(r -> r.getIngredients() == null ? java.util.stream.Stream.empty() : r.getIngredients().stream())
                .map(RecipeIngredient::getUnitUsedId)
                .filter(id -> id != null && !id.isBlank())
                .collect(Collectors.toSet());

        if (!unitUsedIds.isEmpty() && unitTypeRepository != null) {
            for (UnitType u : unitTypeRepository.findAllById(unitUsedIds)) {
                if (u != null && u.getId() != null) {
                    unitById.putIfAbsent(u.getId(), u);
                }
            }
        }

        for (Recipe recipe : recipes) {
            if (recipe == null || recipe.getIngredients() == null) continue;

            for (RecipeIngredient ri : recipe.getIngredients()) {
                if (ri == null) continue;

                String ingId = ri.getIngredientId();
                if (ingId == null || ingId.isBlank()) continue;

                Ingredient ing = ingredientById.get(ingId);
                if (ing == null || ing.getId() == null) continue;

                CategoryIngredient cat = ing.getCategory();
                if (cat == null || cat.getId() == null) continue;

                UnitType defaultUnit = ing.getDefaultUnit();
                if (defaultUnit == null || defaultUnit.getId() == null) continue;

                String usedUnitId = ri.getUnitUsedId();
                if (usedUnitId == null || usedUnitId.isBlank()) continue;

                UnitType usedUnit = unitById.get(usedUnitId);
                if (usedUnit == null || usedUnit.getId() == null) continue;

                double qtyDefault;
                try {
                    qtyDefault = convertToDefaultUnit(ing, usedUnit, ri.getQuantity());
                } catch (IllegalArgumentException ex) {
                    continue;
                }

                totalsInDefaultUnit.merge(new Key(cat.getId(), ing.getId()), qtyDefault, Double::sum);
            }
        }

        return new Aggregation(categoryById, ingredientById, unitById, totalsInDefaultUnit);
    }

    private double convertToDefaultUnit(Ingredient ingredient, UnitType usedUnit, double qty) {
        if (ingredient == null || ingredient.getDefaultUnit() == null || ingredient.getDefaultUnit().getId() == null) {
            throw new IllegalArgumentException("Ingrediente sem unidade padrão");
        }

        if (usedUnit == null || usedUnit.getId() == null) {
            throw new IllegalArgumentException("Unidade usada inválida");
        }

        if (ingredient.getDefaultUnit().getId().equals(usedUnit.getId())) {
            return qty;
        }

        return ingredient.getConversions().stream()
                .filter(c -> c != null
                        && c.getToUnit() != null
                        && c.getToUnit().getId() != null
                        && c.getToUnit().getId().equals(usedUnit.getId()))
                .findFirst()
                .map(c -> qty * c.getFactor())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Não existe conversão de " + usedUnit.getName() + " para " + ingredient.getDefaultUnit().getName()
                ));
    }

    private List<ShoppingListCategoryResponse> toCategoryResponses(Aggregation aggregation) {
        Map<String, ShoppingListCategoryResponse> categoryRespMap = new HashMap<>();

        for (Map.Entry<Key, Double> entry : aggregation.totalsInDefaultUnit().entrySet()) {
            Key key = entry.getKey();
            Double qty = entry.getValue();

            CategoryIngredient cat = aggregation.categoryById().get(key.categoryId());
            Ingredient ing = aggregation.ingredientById().get(key.ingredientId());

            ShoppingListCategoryResponse catResp = categoryRespMap.computeIfAbsent(key.categoryId(), id -> newCategoryResponse(id, cat));
            catResp.getItems().add(toItemResponse(key, ing, qty));
        }

        List<ShoppingListCategoryResponse> result = categoryRespMap.values().stream()
                .sorted(Comparator.comparing(c -> c.getCategoryName() == null ? "" : c.getCategoryName(), String.CASE_INSENSITIVE_ORDER))
                .toList();

        for (ShoppingListCategoryResponse c : result) {
            if (c.getItems() != null) {
                c.getItems().sort(Comparator.comparing(i -> i.getIngredientName() == null ? "" : i.getIngredientName(), String.CASE_INSENSITIVE_ORDER));
            }
        }

        return result;
    }

    private ShoppingListCategoryResponse newCategoryResponse(String categoryId, CategoryIngredient cat) {
        ShoppingListCategoryResponse c = new ShoppingListCategoryResponse();
        c.setCategoryId(categoryId);
        c.setCategoryName(cat != null ? cat.getName() : null);
        return c;
    }

    private ShoppingListItemResponse toItemResponse(Key key, Ingredient ing, Double qty) {
        UnitType defaultUnit = ing != null ? ing.getDefaultUnit() : null;

        ShoppingListItemResponse item = new ShoppingListItemResponse();
        item.setIngredientId(key.ingredientId());
        item.setIngredientName(ing != null ? ing.getName() : null);
        item.setQuantity(roundQuantity(qty));
        item.setUnitId(defaultUnit != null ? defaultUnit.getId() : null);
        item.setUnitName(defaultUnit != null ? defaultUnit.getName() : null);
        item.setUnitAbbreviation(defaultUnit != null ? defaultUnit.getAbbreviation() : null);
        return item;
    }

    private Double roundQuantity(Double value) {
        if (value == null) return null;

        return BigDecimal.valueOf(value)
                .setScale(3, RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .doubleValue();
    }

    private record DateRange(LocalDate start, LocalDate end) {}

    private record Key(String categoryId, String ingredientId) {}

    private record Aggregation(
            Map<String, CategoryIngredient> categoryById,
            Map<String, Ingredient> ingredientById,
            Map<String, UnitType> unitById,
            Map<Key, Double> totalsInDefaultUnit
    ) {}
}
