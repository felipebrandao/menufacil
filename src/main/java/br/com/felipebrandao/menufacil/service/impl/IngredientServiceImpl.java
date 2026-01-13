package br.com.felipebrandao.menufacil.service.impl;

import br.com.felipebrandao.menufacil.dto.ingredient.IngredientRequest;
import br.com.felipebrandao.menufacil.model.Ingredient;
import br.com.felipebrandao.menufacil.model.UnitConversion;
import br.com.felipebrandao.menufacil.repository.CategoryIngredientRepository;
import br.com.felipebrandao.menufacil.repository.IngredientRepository;
import br.com.felipebrandao.menufacil.repository.UnitTypeRepository;
import br.com.felipebrandao.menufacil.service.IngredientService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class IngredientServiceImpl implements IngredientService {

    private static final int MAX_LIMIT = 100;

    private final IngredientRepository repository;
    private final CategoryIngredientRepository categoryIngredientRepository;
    private final UnitTypeRepository unitTypeRepository;

    @Override
    public List<Ingredient> list() {
        return repository.findAll();
    }

    @Override
    public List<Ingredient> search(String query, int limit, String categoryId) {
        int safeLimit = Math.clamp(limit, 1, MAX_LIMIT);
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
    public Ingredient create(IngredientRequest ingredientRequest) {

        if (repository.existsByNameAndCategoryId(ingredientRequest.getName(), ingredientRequest.getCategory())) {
            throw new IllegalArgumentException("Ingrediente com esse nome já existe na categoria");
        }

        var category = categoryIngredientRepository.findById(ingredientRequest.getCategory())
                .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada: " + ingredientRequest.getCategory()));

        var defaultUnit = unitTypeRepository.findById(ingredientRequest.getDefaultUnit())
                .orElseThrow(() -> new IllegalArgumentException("Unidade padrão não encontrada: " + ingredientRequest.getDefaultUnit()));

        var ingredient = new Ingredient();
        ingredient.setName(ingredientRequest.getName());
        ingredient.setCategory(category);
        ingredient.setDefaultUnit(defaultUnit);

        var conversions = new ArrayList<UnitConversion>();
        conversions.add(new UnitConversion(defaultUnit, 1.0));

        if (ingredientRequest.getConversions() != null) {
            var additionalConversions = ingredientRequest.getConversions().stream()
                    .filter(c -> !c.getToUnit().equals(ingredientRequest.getDefaultUnit())) // Evita duplicação
                    .map(c -> {
                        var unit = unitTypeRepository.findById(c.getToUnit())
                                .orElseThrow(() -> new IllegalArgumentException("Unidade não encontrada: " + c.getToUnit()));
                        return new UnitConversion(unit, c.getFactor());
                    }).toList();
            conversions.addAll(additionalConversions);
        }

        ingredient.setConversions(conversions);

        try {
            var saved = repository.save(ingredient);
            return repository.findById(saved.getId()).orElse(saved);
        } catch (DuplicateKeyException e) {
            throw new IllegalArgumentException("Ingrediente com esse nome já existe na categoria");
        }
    }

    @Override
    public Ingredient update(String id, IngredientRequest ingredientRequest) {

        var ingredient = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ingrediente não encontrado: " + id));

        if (repository.existsByNameAndCategoryIdAndIdNot(
                ingredientRequest.getName(),
                ingredientRequest.getCategory(),
                id)) {
            throw new IllegalArgumentException("Ingrediente com esse nome já existe na categoria");
        }

        var category = categoryIngredientRepository.findById(ingredientRequest.getCategory())
                .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada: " + ingredientRequest.getCategory()));
        ingredient.setCategory(category);

        var defaultUnit = unitTypeRepository.findById(ingredientRequest.getDefaultUnit())
                .orElseThrow(() -> new IllegalArgumentException("Unidade padrão não encontrada: " + ingredientRequest.getDefaultUnit()));
        ingredient.setDefaultUnit(defaultUnit);

        ingredient.setName(ingredientRequest.getName());

        var conversions = new ArrayList<UnitConversion>();
        conversions.add(new UnitConversion(defaultUnit, 1.0));

        if (ingredientRequest.getConversions() != null) {
            var additionalConversions = ingredientRequest.getConversions()
                    .stream()
                    .filter(c -> !c.getToUnit().equals(ingredientRequest.getDefaultUnit())) // Evita duplicação
                    .map(c -> {
                        var unit = unitTypeRepository.findById(c.getToUnit())
                                .orElseThrow(() -> new IllegalArgumentException("Unidade não encontrada: " + c.getToUnit()));
                        return new UnitConversion(unit, c.getFactor());
                    })
                    .toList();
            conversions.addAll(additionalConversions);
        }

        ingredient.setConversions(conversions);

        return repository.save(ingredient);
    }


    @Override
    public boolean delete(String id) {
        if (!repository.existsById(id)) return false;
        repository.deleteById(id);
        return true;
    }

    @Override
    public List<Ingredient> autocomplete(String query, int limit) {
        int safeLimit = Math.clamp(limit, 1, MAX_LIMIT);
        var page = PageRequest.of(0, safeLimit);
        return repository.findByNameContainingIgnoreCase(query == null ? "" : query, page);
    }

    @Override
    public Optional<Ingredient> getById(String id) {
        return repository.findById(id);
    }
}
