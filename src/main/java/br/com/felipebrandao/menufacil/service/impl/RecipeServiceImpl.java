package br.com.felipebrandao.menufacil.service.impl;

import br.com.felipebrandao.menufacil.dto.recipe.CreateRecipeRequest;
import br.com.felipebrandao.menufacil.dto.recipe.RecipeCategoryResponse;
import br.com.felipebrandao.menufacil.dto.recipe.RecipeIngredientRequest;
import br.com.felipebrandao.menufacil.dto.recipe.RecipeIngredientResponse;
import br.com.felipebrandao.menufacil.dto.recipe.RecipeResponse;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
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

    @Transactional
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
                .createdAt(Instant.now())
                .build();

        populateRecipeFromDto(recipe,
                request.getName(),
                category,
                request.getIngredients(),
                request.getInstructions(),
                request.getMainImage(),
                request.getGallery());

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
            List<String> gallery
    ) {
        List<RecipeIngredient> mappedIngredients = mapIngredients(ingredientRequests);

        recipe.setName(name);
        recipe.setCategory(category);
        recipe.setIngredients(mappedIngredients);
        recipe.setInstructions(instructions);
        recipe.setMainImage(mainImage);
        recipe.setGallery(gallery);
    }

    private List<RecipeIngredient> mapIngredients(List<RecipeIngredientRequest> requests) {

        Set<String> ingredientIds = requests.stream()
                .map(RecipeIngredientRequest::getIngredientId)
                .collect(Collectors.toSet());

        Set<String> unitIds = requests.stream()
                .map(RecipeIngredientRequest::getUnitUsedId)
                .collect(Collectors.toSet());

        Iterable<Ingredient> foundIngredientsIter = ingredientRepository.findAllById(ingredientIds);
        List<Ingredient> foundIngredients = StreamSupport.stream(foundIngredientsIter.spliterator(), false)
                .toList();

        Iterable<UnitType> foundUnitsIter = unitTypeRepository.findAllById(unitIds);
        List<UnitType> foundUnits = StreamSupport.stream(foundUnitsIter.spliterator(), false)
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
                    double quantityInDefaultUnit = convertToDefaultUnit(
                            ingredient,
                            unitUsed,
                            req.getQuantity()
                    );
                    return RecipeIngredient.builder()
                            .ingredient(ingredient)
                            .unitUsed(unitUsed)
                            .quantity(req.getQuantity())
                            .defaultUnit(ingredient.getDefaultUnit())
                            .quantityInDefaultUnit(quantityInDefaultUnit)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private double convertToDefaultUnit(Ingredient ingredient, UnitType usedUnit, double qty) {

        if (ingredient.getDefaultUnit().getId().equals(usedUnit.getId())) {
            return qty;
        }

        return ingredient.getConversions().stream()
                .filter(c -> c.getToUnit().getId().equals(usedUnit.getId()))
                .findFirst()
                .map(c -> qty * c.getFactor())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Não existe conversão de " + usedUnit.getName() + " para " + ingredient.getDefaultUnit().getName()
                ));
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

    private RecipeResponse mapRecipeResponse(Recipe recipe) {
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
                        recipe.getIngredients().stream()
                                .map(ri -> RecipeIngredientResponse.builder()
                                        .ingredientId(ri.getIngredient().getId())
                                        .ingredientName(ri.getIngredient().getName())
                                        .unitUsed(ri.getUnitUsed().getName())
                                        .quantity(ri.getQuantity())
                                        .defaultUnit(ri.getDefaultUnit().getName())
                                        .quantityInDefaultUnit(ri.getQuantityInDefaultUnit())
                                        .build()
                                )
                                .toList()
                )
                .instructions(recipe.getInstructions())
                .mainImage(recipe.getMainImage())
                .gallery(recipe.getGallery())
                .createdAt(recipe.getCreatedAt())
                .build();
    }

    @Transactional
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
                request.getGallery()
        );

        recipeRepository.save(recipe);

        return mapRecipeResponse(recipe);
    }

}
