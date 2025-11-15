package br.com.felipebrandao.menufacil.service.impl;

import br.com.felipebrandao.menufacil.model.CategoryIngredient;
import br.com.felipebrandao.menufacil.repository.CategoryIngredientRepository;
import br.com.felipebrandao.menufacil.service.CategoryIngredientService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryIngredientServiceImpl implements CategoryIngredientService {

    private final CategoryIngredientRepository repository;

    @Override
    public List<CategoryIngredient> list() {
        return repository.findAll();
    }

    @Override
    public CategoryIngredient create(CategoryIngredient category) {
        if (repository.existsByName(category.getName())) {
            throw new IllegalArgumentException("Nome da categoria já existe");
        }
        try {
            return repository.save(category);
        } catch (DuplicateKeyException e) {
            throw new IllegalArgumentException("Nome da categoria já existe");
        }
    }

    @Override
    public Optional<CategoryIngredient> update(String id, CategoryIngredient category) {
        return repository.findById(id)
                .map(existing -> {
                    repository.findByName(category.getName())
                            .ifPresent(conflict -> {
                                if (!conflict.getId().equals(id)) {
                                    throw new IllegalArgumentException("Nome da categoria já existe");
                                }
                            });
                    category.setId(id);
                    return repository.save(category);
                });
    }

    @Override
    public boolean delete(String id) {
        if (!repository.existsById(id)) return false;
        repository.deleteById(id);
        return true;
    }
}
