package br.com.felipebrandao.menufacil.controller;

import br.com.felipebrandao.menufacil.dto.shopping.ShoppingListResponse;
import br.com.felipebrandao.menufacil.service.ShoppingListService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/shopping-list")
@RequiredArgsConstructor
public class ShoppingListController {

    private final ShoppingListService shoppingListService;

    @GetMapping
    public ResponseEntity<ShoppingListResponse> generate(
            @RequestParam String view,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month
    ) {
        ShoppingListResponse resp = shoppingListService.generate(view, start, year, month);

        if (resp.getCategories() == null || resp.getCategories().isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(resp);
    }
}
