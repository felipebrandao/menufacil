package br.com.felipebrandao.menufacil.service;

import br.com.felipebrandao.menufacil.model.Ingredient;

import java.util.List;
import java.util.Optional;

public interface IngredientService {
    List<Ingredient> list();
    List<Ingredient> search(String query, int limit, String categoryId);
    Ingredient create(Ingredient ingredient);
    Optional<Ingredient> update(String id, Ingredient ingredient);
    boolean delete(String id);
    List<Ingredient> autocomplete(String query, int limit);
}
