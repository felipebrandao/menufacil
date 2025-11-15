package br.com.felipebrandao.menufacil.controller;

import br.com.felipebrandao.menufacil.model.Category;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {


    @GetMapping
    public ResponseEntity<List<Category>> listAll() {
        return null;
    }

    @PostMapping
    public ResponseEntity<Category> create(@RequestBody Category category) {
        return null;
    }
}
