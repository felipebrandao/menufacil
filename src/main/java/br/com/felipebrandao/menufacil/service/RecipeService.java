package br.com.felipebrandao.menufacil.service;

import br.com.felipebrandao.menufacil.dto.recipe.CreateRecipeRequest;
import br.com.felipebrandao.menufacil.dto.recipe.RecipeListResponse;
import br.com.felipebrandao.menufacil.dto.recipe.RecipeResponse;
import br.com.felipebrandao.menufacil.dto.recipe.UpdateRecipeRequest;

import java.util.List;

public interface RecipeService {

    RecipeResponse create(CreateRecipeRequest request);
    RecipeResponse getById(String id);
    List<RecipeResponse> listAll();
    void delete(String id);
    RecipeResponse update(String id, UpdateRecipeRequest request);

    RecipeListResponse listRecipes(String query, String category, int page, int limit);
}
