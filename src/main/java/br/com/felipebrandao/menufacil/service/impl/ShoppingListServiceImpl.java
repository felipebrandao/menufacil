package br.com.felipebrandao.menufacil.service.impl;

import br.com.felipebrandao.menufacil.dto.shopping.ShoppingListCategoryResponse;
import br.com.felipebrandao.menufacil.dto.shopping.ShoppingListItemResponse;
import br.com.felipebrandao.menufacil.dto.shopping.ShoppingListResponse;
import br.com.felipebrandao.menufacil.model.CategoryIngredient;
import br.com.felipebrandao.menufacil.model.Ingredient;
import br.com.felipebrandao.menufacil.model.Recipe;
import br.com.felipebrandao.menufacil.model.RecipeIngredient;
import br.com.felipebrandao.menufacil.model.ScheduleDay;
import br.com.felipebrandao.menufacil.model.ScheduledRecipe;
import br.com.felipebrandao.menufacil.model.UnitType;
import br.com.felipebrandao.menufacil.repository.RecipeRepository;
import br.com.felipebrandao.menufacil.repository.ScheduleRepository;
import br.com.felipebrandao.menufacil.service.ShoppingListService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

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
@RequiredArgsConstructor
public class ShoppingListServiceImpl implements ShoppingListService {

    private final ScheduleRepository scheduleRepository;
    private final RecipeRepository recipeRepository;

    @Override
    public ShoppingListResponse generate(String view, LocalDate start, Integer year, Integer month) {
        DateRange range = resolveRange(view, start, year, month);

        List<ScheduleDay> days = scheduleRepository.findByDateRange(range.start(), range.end());
        Set<String> recipeIds = extractRecipeIds(days);

        List<Recipe> recipes = recipeIds.isEmpty()
                ? Collections.emptyList()
                : recipeRepository.findAllById(recipeIds);

        Aggregation aggregation = aggregateInDefaultUnit(recipes);
        List<ShoppingListCategoryResponse> categories = toCategoryResponses(aggregation);

        ShoppingListResponse resp = new ShoppingListResponse();
        resp.setView(view);
        resp.setStart(range.start());
        resp.setEnd(range.end());
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

    private Set<String> extractRecipeIds(List<ScheduleDay> days) {
        return days.stream()
                .filter(Objects::nonNull)
                .flatMap(d -> d.getRecipes() == null ? java.util.stream.Stream.empty() : d.getRecipes().stream())
                .map(ScheduledRecipe::getRecipeId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private Aggregation aggregateInDefaultUnit(List<Recipe> recipes) {
        Map<String, CategoryIngredient> categoryById = new HashMap<>();
        Map<String, Ingredient> ingredientById = new HashMap<>();
        Map<Key, Double> totalsInDefaultUnit = new HashMap<>();

        for (Recipe recipe : recipes) {
            if (recipe == null || recipe.getIngredients() == null) continue;

            for (RecipeIngredient ri : recipe.getIngredients()) {
                ResolvedItem item = resolveItemInDefaultUnit(ri);
                if (item == null) continue;

                categoryById.putIfAbsent(item.category().getId(), item.category());
                ingredientById.putIfAbsent(item.ingredient().getId(), item.ingredient());

                totalsInDefaultUnit.merge(new Key(item.category().getId(), item.ingredient().getId()), item.qtyDefault(), Double::sum);
            }
        }

        return new Aggregation(categoryById, ingredientById, totalsInDefaultUnit);
    }

    /**
     * Transforma um RecipeIngredient em um item consolidável na unidade padrão.
     * Retorna null se não houver dados suficientes.
     */
    private ResolvedItem resolveItemInDefaultUnit(RecipeIngredient ri) {
        if (ri == null || ri.getIngredient() == null) return null;

        Ingredient ing = ri.getIngredient();
        if (ing.getId() == null) return null;

        CategoryIngredient cat = ing.getCategory();
        if (cat == null || cat.getId() == null) return null;

        UnitType defaultUnit = ing.getDefaultUnit();
        if (defaultUnit == null || defaultUnit.getId() == null) return null;

        Double qtyDefault = ri.getQuantityInDefaultUnit();
        if (qtyDefault == null) {
            // fallback: se vier nulo (dados antigos), tenta usar quantity se já estiver em defaultUnit
            if (ri.getUnitUsed() != null
                    && ri.getUnitUsed().getId() != null
                    && ri.getUnitUsed().getId().equals(defaultUnit.getId())
                    && ri.getQuantity() != null) {
                qtyDefault = ri.getQuantity();
            } else {
                return null;
            }
        }

        return new ResolvedItem(cat, ing, defaultUnit, qtyDefault);
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

        return categoryRespMap.values().stream()
                .sorted(Comparator.comparing(c -> c.getCategoryName() == null ? "" : c.getCategoryName(), String.CASE_INSENSITIVE_ORDER))
                .peek(c -> c.getItems().sort(Comparator.comparing(i -> i.getIngredientName() == null ? "" : i.getIngredientName(), String.CASE_INSENSITIVE_ORDER)))
                .toList();
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
        item.setQuantity(qty);
        item.setUnitId(defaultUnit != null ? defaultUnit.getId() : null);
        item.setUnitName(defaultUnit != null ? defaultUnit.getName() : null);
        item.setUnitAbbreviation(defaultUnit != null ? defaultUnit.getAbbreviation() : null);
        return item;
    }

    private record DateRange(LocalDate start, LocalDate end) {}

    private record Key(String categoryId, String ingredientId) {}

    private record ResolvedItem(CategoryIngredient category, Ingredient ingredient, UnitType defaultUnit, double qtyDefault) {}

    private record Aggregation(
            Map<String, CategoryIngredient> categoryById,
            Map<String, Ingredient> ingredientById,
            Map<Key, Double> totalsInDefaultUnit
    ) {}
}
