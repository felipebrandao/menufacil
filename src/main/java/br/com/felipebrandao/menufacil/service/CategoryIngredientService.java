package br.com.felipebrandao.menufacil.service;

import br.com.felipebrandao.menufacil.model.CategoryIngredient;

import java.util.List;
import java.util.Optional;

public interface CategoryIngredientService {
    List<CategoryIngredient> list();
    CategoryIngredient create(CategoryIngredient category);
    Optional<CategoryIngredient> update(String id, CategoryIngredient category);
    boolean delete(String id);
}
