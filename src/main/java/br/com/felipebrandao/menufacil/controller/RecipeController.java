package br.com.felipebrandao.menufacil.controller;

import br.com.felipebrandao.menufacil.dto.recipe.CreateRecipeRequest;
import br.com.felipebrandao.menufacil.dto.recipe.RecipeResponse;
import br.com.felipebrandao.menufacil.dto.recipe.UpdateRecipeRequest;
import br.com.felipebrandao.menufacil.service.RecipeService;
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

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/recipes")
@RequiredArgsConstructor
public class RecipeController {

    private final RecipeService recipeService;

    @PostMapping
    public ResponseEntity<RecipeResponse> create(@Valid @RequestBody CreateRecipeRequest request) {
        RecipeResponse response = recipeService.create(request);
        return ResponseEntity.created(URI.create("/api/recipes/" + response.getId())).body(response);
    }

    @GetMapping
    public List<RecipeResponse> listAll() {
        return recipeService.listAll();
    }

    @GetMapping("/{id}")
    public RecipeResponse getById(@PathVariable String id) {
        return recipeService.getById(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        recipeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public RecipeResponse update(
            @PathVariable String id,
            @Valid @RequestBody UpdateRecipeRequest request) {
        return recipeService.update(id, request);
    }

}
