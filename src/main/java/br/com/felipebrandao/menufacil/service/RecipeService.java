package br.com.felipebrandao.menufacil.service;

import br.com.felipebrandao.menufacil.model.Recipe;
import br.com.felipebrandao.menufacil.repository.RecipeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecipeService {

    private final RecipeRepository repository;

    public RecipeService(RecipeRepository repository) {
        this.repository = repository;
    }

    public Recipe create(Recipe recipe) {
        return repository.save(recipe);
    }

    public List<Recipe> findAll() {
        return repository.findAll();
    }

    public Recipe findById(String id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Recipe not found"));
    }

    public void delete(String id) {
        repository.deleteById(id);
    }

    public List<Recipe> searchByName(String query) {
        return repository.findByNameContainingIgnoreCase(query);
    }

    public List<Recipe> findByCategory(String category) {
        return repository.findByCategoryId(category);
    }

}

