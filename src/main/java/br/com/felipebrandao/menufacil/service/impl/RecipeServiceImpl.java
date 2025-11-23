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
import java.util.NoSuchElementException;

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

        List<RecipeIngredient> mappedIngredients = request.getIngredients().stream()
                .map(this::mapIngredient)
                .toList();

        Recipe recipe = Recipe.builder()
                .name(request.getName())
                .category(category)
                .ingredients(mappedIngredients)
                .instructions(request.getInstructions())
                .mainImage(request.getMainImage())
                .gallery(request.getGallery())
                .createdAt(Instant.now())
                .build();

        recipeRepository.save(recipe);

        return mapRecipeResponse(recipe);
    }

    private RecipeIngredient mapIngredient(RecipeIngredientRequest req) {

        Ingredient ingredient = ingredientRepository.findById(req.getIngredientId())
                .orElseThrow(() -> new NoSuchElementException("Ingrediente não encontrado"));

        UnitType unitUsed = unitTypeRepository.findById(req.getUnitUsedId())
                .orElseThrow(() -> new NoSuchElementException("Unidade informada não existe"));

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

        List<RecipeIngredient> mappedIngredients = request.getIngredients().stream()
                .map(this::mapIngredient)
                .toList();

        recipe.setName(request.getName());
        recipe.setCategory(category);
        recipe.setIngredients(mappedIngredients);
        recipe.setInstructions(request.getInstructions());
        recipe.setMainImage(request.getMainImage());
        recipe.setGallery(request.getGallery());

        recipeRepository.save(recipe);

        return mapRecipeResponse(recipe);
    }

}
