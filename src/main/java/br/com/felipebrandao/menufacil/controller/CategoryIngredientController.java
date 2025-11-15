package br.com.felipebrandao.menufacil.controller;

import br.com.felipebrandao.menufacil.model.CategoryIngredient;
import br.com.felipebrandao.menufacil.service.CategoryIngredientService;
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
@RequestMapping("/api/categories-ingredient")
@RequiredArgsConstructor
public class CategoryIngredientController {

    private final CategoryIngredientService categoryIngredientService;

    @GetMapping
    public List<CategoryIngredient> list() {
        return categoryIngredientService.list();
    }

    @PostMapping
    public CategoryIngredient create(@Valid @RequestBody CategoryIngredient categoryIngredient) {
        return categoryIngredientService.create(categoryIngredient);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryIngredient> update(@PathVariable String id, @Valid @RequestBody CategoryIngredient category) {
        return categoryIngredientService.update(id, category)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        if (!categoryIngredientService.delete(id)) return ResponseEntity.notFound().build();
        return ResponseEntity.noContent().build();
    }
}
