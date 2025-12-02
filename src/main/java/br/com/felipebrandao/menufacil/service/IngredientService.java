package br.com.felipebrandao.menufacil.service;

import br.com.felipebrandao.menufacil.dto.ingredient.IngredientRequest;
import br.com.felipebrandao.menufacil.model.Ingredient;

import java.util.List;
import java.util.Optional;

public interface IngredientService {
    List<Ingredient> list();
    List<Ingredient> search(String query, int limit, String categoryId);
    Ingredient create(IngredientRequest ingredientRequest);
    Ingredient update(String id, IngredientRequest ingredientRequest);
    boolean delete(String id);
    List<Ingredient> autocomplete(String query, int limit);
    Optional<Ingredient> getById(String id);
}
