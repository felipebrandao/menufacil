package br.com.felipebrandao.menufacil.controller;

import br.com.felipebrandao.menufacil.dto.ingredient.IngredientRequest;
import br.com.felipebrandao.menufacil.model.Ingredient;
import br.com.felipebrandao.menufacil.service.IngredientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ingredients")
@RequiredArgsConstructor
public class IngredientController {

    private final IngredientService ingredientService;

    @GetMapping
    public List<Ingredient> list(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String categoryId,
            @RequestParam(defaultValue = "5") int limit) {
        if ((query == null || query.isBlank()) && (categoryId == null || categoryId.isBlank())) {
            return ingredientService.list();
        }
        return ingredientService.search(query, limit, categoryId);
    }

    @PostMapping
    public ResponseEntity<Ingredient> create(@Valid @RequestBody IngredientRequest ingredientRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ingredientService.create(ingredientRequest));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Ingredient> update(@PathVariable String id, @Valid @RequestBody IngredientRequest ingredientRequest) {
        return ResponseEntity.ok(ingredientService.update(id, ingredientRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        if(!ingredientService.delete(id)) return ResponseEntity.notFound().build();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public List<Ingredient> searchAutocomplete(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int limit) {
        return ingredientService.autocomplete(query, limit);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ingredient> getById(@PathVariable String id) {
        return ingredientService.getById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
