package br.com.felipebrandao.menufacil.service.impl;

import br.com.felipebrandao.menufacil.model.RecipeCategory;
import br.com.felipebrandao.menufacil.repository.RecipeCategoryRepository;
import br.com.felipebrandao.menufacil.service.RecipeCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RecipeCategoryServiceImpl implements RecipeCategoryService {

    private final RecipeCategoryRepository recipeCategoryRepository;

    @Override
    public List<RecipeCategory> list() {
        return recipeCategoryRepository.findAll();
    }

    @Override
    public RecipeCategory create(RecipeCategory recipeCategory) {
        if(recipeCategoryRepository.existsByName(recipeCategory.getName())) {
            throw new IllegalArgumentException("Nome da categoria já existe");
        }
        return recipeCategoryRepository.save(recipeCategory);
    }

    @Override
    public Optional<RecipeCategory> update(String id, RecipeCategory recipeCategory) {
        return recipeCategoryRepository.findById(id)
                .map(existing -> {
                    recipeCategoryRepository.findByName(recipeCategory.getName())
                            .ifPresent(conflict -> {
                                if (!conflict.getId().equals(id)) {
                                    throw new IllegalArgumentException("Nome da categoria já existe");
                                }
                            });
                    recipeCategory.setId(id);
                    return recipeCategoryRepository.save(recipeCategory);
                });
    }

    @Override
    public boolean delete(String id) {
        if(!recipeCategoryRepository.existsById(id)) return false;
        recipeCategoryRepository.deleteById(id);
        return true;
    }
}
