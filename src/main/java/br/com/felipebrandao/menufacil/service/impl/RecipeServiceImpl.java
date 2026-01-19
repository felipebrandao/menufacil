package br.com.felipebrandao.menufacil.service.impl;

import br.com.felipebrandao.menufacil.dto.Pagination;
import br.com.felipebrandao.menufacil.dto.recipe.CreateRecipeRequest;
import br.com.felipebrandao.menufacil.dto.recipe.RecipeCategoryResponse;
import br.com.felipebrandao.menufacil.dto.recipe.RecipeIngredientRequest;
import br.com.felipebrandao.menufacil.dto.recipe.RecipeIngredientResponse;
import br.com.felipebrandao.menufacil.dto.recipe.RecipeListResponse;
import br.com.felipebrandao.menufacil.dto.recipe.RecipeResponse;
import br.com.felipebrandao.menufacil.dto.recipe.RecipeSummaryResponse;
import br.com.felipebrandao.menufacil.dto.recipe.UpdateRecipeRequest;
import br.com.felipebrandao.menufacil.model.Ingredient;
import br.com.felipebrandao.menufacil.model.Recipe;
import br.com.felipebrandao.menufacil.model.RecipeCategory;
import br.com.felipebrandao.menufacil.model.RecipeIngredient;
import br.com.felipebrandao.menufacil.model.UnitType;
import br.com.felipebrandao.menufacil.repository.IngredientRepository;
import br.com.felipebrandao.menufacil.repository.RecipeCategoryRepository;
import br.com.felipebrandao.menufacil.repository.RecipeRepository;
import br.com.felipebrandao.menufacil.repository.UnitTypeRepository;
import br.com.felipebrandao.menufacil.service.RecipeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class RecipeServiceImpl implements RecipeService {

    private final RecipeRepository recipeRepository;
    private final IngredientRepository ingredientRepository;
    private final UnitTypeRepository unitTypeRepository;
    private final RecipeCategoryRepository recipeCategoryRepository;

    public RecipeResponse create(CreateRecipeRequest request) {

        RecipeCategory category = recipeCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new NoSuchElementException("Categoria não encontrada"));

        Recipe recipe = Recipe.builder()
                .name(request.getName())
                .category(category)
                .ingredients(List.of())
                .instructions(request.getInstructions())
                .mainImage(request.getMainImage())
                .gallery(request.getGallery())
                .build();

        populateRecipeFromDto(recipe,
                request.getName(),
                category,
                request.getIngredients(),
                request.getInstructions(),
                request.getMainImage(),
                request.getGallery(),
                request.getTotalTime(),
                request.getHighlighted());

        recipeRepository.save(recipe);

        return mapRecipeResponse(recipe);
    }

    private void populateRecipeFromDto(
            Recipe recipe,
            String name,
            RecipeCategory category,
            List<RecipeIngredientRequest> ingredientRequests,
            List<String> instructions,
            String mainImage,
            List<String> gallery,
            Integer totalTime,
            Boolean highlighted
    ) {
        List<RecipeIngredient> mappedIngredients = mapIngredients(ingredientRequests);

        recipe.setName(name);
        recipe.setCategory(category);
        recipe.setIngredients(mappedIngredients);
        recipe.setInstructions(instructions);
        recipe.setMainImage(mainImage);
        recipe.setGallery(gallery);
        recipe.setTotalTime(totalTime);
        recipe.setHighlighted(highlighted != null && highlighted);
    }

    private List<RecipeIngredient> mapIngredients(List<RecipeIngredientRequest> requests) {

        if (requests == null || requests.isEmpty()) {
            throw new IllegalArgumentException("A receita deve ter pelo menos um ingrediente.");
        }

        for (int i = 0; i < requests.size(); i++) {
            RecipeIngredientRequest req = requests.get(i);
            if (req == null) {
                throw new IllegalArgumentException("Ingrediente da receita inválido na posição " + i);
            }
            if (req.getIngredientId() == null || req.getIngredientId().isBlank()) {
                throw new IllegalArgumentException("ingredientId é obrigatório (posição " + i + ")");
            }
            if (req.getUnitUsedId() == null || req.getUnitUsedId().isBlank()) {
                throw new IllegalArgumentException("unitUsedId é obrigatório (posição " + i + ")");
            }
            if (req.getQuantity() == null) {
                throw new IllegalArgumentException("quantity é obrigatório (posição " + i + ")");
            }
            if (req.getQuantity() <= 0.0d) {
                throw new IllegalArgumentException("quantity deve ser maior que 0 (posição " + i + ")");
            }
        }

        Set<String> ingredientIds = requests.stream()
                .map(RecipeIngredientRequest::getIngredientId)
                .collect(Collectors.toSet());

        Set<String> unitIds = requests.stream()
                .map(RecipeIngredientRequest::getUnitUsedId)
                .collect(Collectors.toSet());

        List<Ingredient> foundIngredients = StreamSupport.stream(ingredientRepository.findAllById(ingredientIds).spliterator(), false)
                .toList();

        List<UnitType> foundUnits = StreamSupport.stream(unitTypeRepository.findAllById(unitIds).spliterator(), false)
                .toList();

        Map<String, Ingredient> ingredientMap = foundIngredients.stream()
                .collect(Collectors.toMap(Ingredient::getId, i -> i));

        Map<String, UnitType> unitMap = foundUnits.stream()
                .collect(Collectors.toMap(UnitType::getId, u -> u));

        for (String id : ingredientIds) {
            if (!ingredientMap.containsKey(id)) {
                throw new NoSuchElementException("Ingrediente não encontrado: " + id);
            }
        }
        for (String id : unitIds) {
            if (!unitMap.containsKey(id)) {
                throw new NoSuchElementException("Unidade informada não existe: " + id);
            }
        }

        return requests.stream()
                .map(req -> {
                    Ingredient ingredient = ingredientMap.get(req.getIngredientId());
                    UnitType unitUsed = unitMap.get(req.getUnitUsedId());

                    validateConversionExists(ingredient, unitUsed);

                    return RecipeIngredient.builder()
                            .ingredientId(ingredient.getId())
                            .unitUsedId(unitUsed.getId())
                            .quantity(req.getQuantity())
                            .build();
                })
                .toList();
    }

    private void validateConversionExists(Ingredient ingredient, UnitType usedUnit) {
        if (ingredient == null || ingredient.getDefaultUnit() == null || ingredient.getDefaultUnit().getId() == null) {
            throw new IllegalArgumentException("Ingrediente sem unidade padrão");
        }
        if (usedUnit == null || usedUnit.getId() == null) {
            throw new IllegalArgumentException("Unidade usada inválida");
        }

        if (ingredient.getDefaultUnit().getId().equals(usedUnit.getId())) {
            return;
        }

        boolean hasConversion = ingredient.getConversions() != null && ingredient.getConversions().stream()
                .anyMatch(c -> c != null
                        && c.getToUnit() != null
                        && usedUnit.getId().equals(c.getToUnit().getId()));

        if (!hasConversion) {
            throw new IllegalArgumentException(
                    "Não existe conversão de " + ingredient.getDefaultUnit().getName() + " para " + usedUnit.getName()
            );
        }
    }

    public RecipeResponse getById(String id) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Receita não encontrada"));
        return mapRecipeResponse(recipe);
    }

    public List<RecipeResponse> listAll() {
        return recipeRepository.findAll()
                .stream()
                .map(this::mapRecipeResponse)
                .toList();
    }

    public void delete(String id) {
        if (!recipeRepository.existsById(id)) {
            throw new NoSuchElementException("Receita não encontrada");
        }
        recipeRepository.deleteById(id);
    }

    @Override
    public RecipeResponse update(String id, UpdateRecipeRequest request) {

        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Receita não encontrada"));

        RecipeCategory category = recipeCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new NoSuchElementException("Categoria não encontrada"));

        populateRecipeFromDto(
                recipe,
                request.getName(),
                category,
                request.getIngredients(),
                request.getInstructions(),
                request.getMainImage(),
                request.getGallery(),
                request.getTotalTime(),
                request.getHighlighted()
        );

        recipeRepository.save(recipe);

        return mapRecipeResponse(recipe);
    }

    @Override
    public RecipeListResponse listRecipes(String query, String categoryId, int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by("createdAt").descending());

        boolean hasQuery = query != null && !query.isBlank();
        boolean hasCategoryId = categoryId != null && !categoryId.isBlank();

        Page<Recipe> result;

        if (hasQuery || hasCategoryId) {
            result = recipeRepository.search(
                    hasQuery ? query : "",
                    hasCategoryId ? categoryId : "",
                    pageable
            );
        } else {
            result = recipeRepository.findAll(pageable);
        }

        List<RecipeSummaryResponse> list = result
                .getContent()
                .stream()
                .map(this::toSummary)
                .toList();

        RecipeListResponse response = new RecipeListResponse();
        response.setRecipes(list);

        Pagination pagination = new Pagination();
        pagination.setPage(page);
        pagination.setLimit(limit);
        pagination.setTotal(result.getTotalElements());
        pagination.setTotalPages(result.getTotalPages());
        response.setPagination(pagination);

        return response;
    }

    private RecipeResponse mapRecipeResponse(Recipe recipe) {
        List<RecipeIngredient> ris = recipe.getIngredients() != null ? recipe.getIngredients() : List.of();

        Set<String> ingredientIds = ris.stream()
                .map(RecipeIngredient::getIngredientId)
                .filter(id -> id != null && !id.isBlank())
                .collect(Collectors.toSet());

        Set<String> unitIds = ris.stream()
                .map(RecipeIngredient::getUnitUsedId)
                .filter(id -> id != null && !id.isBlank())
                .collect(Collectors.toSet());

        Map<String, Ingredient> ingredientMap = ingredientIds.isEmpty()
                ? Map.of()
                : StreamSupport.stream(ingredientRepository.findAllById(ingredientIds).spliterator(), false)
                .collect(Collectors.toMap(Ingredient::getId, i -> i));

        Map<String, UnitType> unitMap = unitIds.isEmpty()
                ? Map.of()
                : StreamSupport.stream(unitTypeRepository.findAllById(unitIds).spliterator(), false)
                .collect(Collectors.toMap(UnitType::getId, u -> u));

        return RecipeResponse.builder()
                .id(recipe.getId())
                .name(recipe.getName())
                .category(
                        RecipeCategoryResponse.builder()
                                .id(recipe.getCategory().getId())
                                .name(recipe.getCategory().getName())
                                .build()
                )
                .ingredients(
                        ris.stream()
                                .map(ri -> {
                                    String ingId = ri.getIngredientId();
                                    String unitId = ri.getUnitUsedId();

                                    Ingredient ing = ingId != null ? ingredientMap.get(ingId) : null;
                                    UnitType unit = unitId != null ? unitMap.get(unitId) : null;

                                    return RecipeIngredientResponse.builder()
                                            .ingredientId(ingId)
                                            .ingredientName(ing != null ? ing.getName() : null)
                                            .unitUsedId(unitId)
                                            .unitUsedName(unit != null ? unit.getName() : null)
                                            .unitUsedAbbreviation(unit != null ? unit.getAbbreviation() : null)
                                            .quantity(ri.getQuantity())
                                            .build();
                                })
                                .toList()
                )
                .instructions(recipe.getInstructions())
                .mainImage(recipe.getMainImage())
                .gallery(recipe.getGallery())
                .totalTime(recipe.getTotalTime())
                .highlighted(recipe.getHighlighted())
                .createdAt(recipe.getCreatedAt())
                .updatedAt(recipe.getUpdatedAt())
                .build();
    }

    private RecipeSummaryResponse toSummary(Recipe r) {
        RecipeSummaryResponse dto = new RecipeSummaryResponse();
        dto.setId(r.getId());
        dto.setName(r.getName());
        dto.setCategory(r.getCategory().getName());
        dto.setMainImage(r.getMainImage());
        dto.setRating(0.0);
        dto.setTotalTime(r.getTotalTime());
        dto.setHighlighted(r.getHighlighted());
        dto.setCreatedAt(r.getCreatedAt());
        return dto;
    }

}
