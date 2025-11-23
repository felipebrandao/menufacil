package br.com.felipebrandao.menufacil.service;

import br.com.felipebrandao.menufacil.model.RecipeCategory;

import java.util.List;
import java.util.Optional;

public interface RecipeCategoryService {

    List<RecipeCategory> list();
    RecipeCategory create(RecipeCategory recipeCategory);
    Optional<RecipeCategory> update(String id, RecipeCategory recipeCategory);
    boolean delete(String id);
}
