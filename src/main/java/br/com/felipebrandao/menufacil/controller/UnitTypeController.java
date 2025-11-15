package br.com.felipebrandao.menufacil.controller;

import br.com.felipebrandao.menufacil.model.UnitType;
import br.com.felipebrandao.menufacil.service.UnitTypeService;
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
@RequestMapping("/api/units")
@RequiredArgsConstructor
public class UnitTypeController {

    private final UnitTypeService unitTypeService;

    @GetMapping
    public List<UnitType> list() {
        return unitTypeService.list();
    }

    @PostMapping
    public UnitType create(@RequestBody UnitType unit) {
        return unitTypeService.create(unit);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UnitType> update(@PathVariable String id, @RequestBody UnitType unit) {
        return unitTypeService.update(id, unit)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        if (!unitTypeService.delete(id)) return ResponseEntity.notFound().build();
        return ResponseEntity.noContent().build();
    }
}
