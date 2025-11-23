package br.com.felipebrandao.menufacil.controller;

import br.com.felipebrandao.menufacil.model.RecipeCategory;
import br.com.felipebrandao.menufacil.service.RecipeCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/recipe-categories")
@RequiredArgsConstructor
public class RecipeCategoryController {

    private final RecipeCategoryService recipeCategoryService;

    @GetMapping
    public List<RecipeCategory> list() {
        return recipeCategoryService.list();
    }

    @PostMapping
    public RecipeCategory create(@Valid @RequestBody RecipeCategory recipeCategory) {
        return recipeCategoryService.create(recipeCategory);
    }

    @PutMapping({"/{id}"})
    public ResponseEntity<RecipeCategory> update(@PathVariable String id, @Valid @RequestBody RecipeCategory category) {
        return recipeCategoryService.update(id, category)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        if (!recipeCategoryService.delete(id)) return ResponseEntity.notFound().build();
        return ResponseEntity.noContent().build();
    }
}
