package br.com.felipebrandao.menufacil.controller;

import br.com.felipebrandao.menufacil.model.Recipe;
import br.com.felipebrandao.menufacil.service.RecipeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    private final RecipeService service;

    public RecipeController(RecipeService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Recipe> create(@RequestBody Recipe recipe) {
        return ResponseEntity.status(201).body(service.create(recipe));
    }

    @GetMapping
    public ResponseEntity<List<Recipe>> listAll(@RequestParam(required = false) String query,
                                                @RequestParam(required = false) String category) {
        if (query != null) return ResponseEntity.ok(service.searchByName(query));
        if (category != null) return ResponseEntity.ok(service.findByCategory(category));
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Recipe> getById(@PathVariable String id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}

