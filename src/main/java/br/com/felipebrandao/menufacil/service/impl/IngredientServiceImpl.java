package br.com.felipebrandao.menufacil.service.impl;

import br.com.felipebrandao.menufacil.model.Ingredient;
import br.com.felipebrandao.menufacil.repository.IngredientRepository;
import br.com.felipebrandao.menufacil.service.IngredientService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IngredientServiceImpl implements IngredientService {

    private final IngredientRepository repository;

    @Override
    public List<Ingredient> list() {
        return repository.findAll();
    }

    @Override
    public List<Ingredient> search(String query, int limit, String categoryId) {
        int safeLimit = Math.max(1, limit);
        PageRequest page = PageRequest.of(0, safeLimit);
        if (query == null || query.isBlank()) {
            if (categoryId != null && !categoryId.isBlank()) {
                return repository.findByCategoryId(categoryId);
            } else {
                return repository.findAll(page).getContent();
            }
        } else {
            if (categoryId != null && !categoryId.isBlank()) {
                return repository.findByNameContainingIgnoreCaseAndCategoryId(query, categoryId, page);
            } else {
                return repository.findByNameContainingIgnoreCase(query, page);
            }
        }
    }

    @Override
    public Ingredient create(Ingredient ingredient) {
        var catId = ingredient.getCategory() != null ? ingredient.getCategory().getId() : null;
        if (catId != null && repository.existsByNameAndCategoryId(ingredient.getName(), catId)) {
            throw new IllegalArgumentException("Ingrediente com esse nome já existe na categoria");
        }
        try {
            var saved = repository.save(ingredient);
            return repository.findById(saved.getId()).orElse(saved);
        } catch (DuplicateKeyException e) {
            throw new IllegalArgumentException("Ingrediente com esse nome já existe na categoria");
        }
    }

    @Override
    public Optional<Ingredient> update(String id, Ingredient ingredient) {
        return repository.findById(id)
                .map(existing -> {
                    var catId = ingredient.getCategory() != null ? ingredient.getCategory().getId() : null;
                    if (catId != null) {
                        repository.findByNameAndCategoryId(ingredient.getName(), catId)
                                .ifPresent(conflict -> {
                                    if (!conflict.getId().equals(id)) {
                                        throw new IllegalArgumentException("Ingrediente com esse nome já existe na categoria");
                                    }
                                });
                    }
                    ingredient.setId(id);
                    try {
                        var saved = repository.save(ingredient);
                        return repository.findById(saved.getId()).orElse(saved);
                    } catch (DuplicateKeyException e) {
                        throw new IllegalArgumentException("Ingrediente com esse nome já existe na categoria");
                    }
                });
    }

    @Override
    public boolean delete(String id) {
        if (!repository.existsById(id)) return false;
        repository.deleteById(id);
        return true;
    }

    @Override
    public List<Ingredient> autocomplete(String query, int limit) {
        int safeLimit = Math.max(1, limit);
        var page = PageRequest.of(0, safeLimit);
        var ingredients = repository.findByNameContainingIgnoreCase(query == null ? "" : query, page);

        return ingredients;
    }

    @Override
    public Optional<Ingredient> getById(String id) {
        return repository.findById(id);
    }
}
