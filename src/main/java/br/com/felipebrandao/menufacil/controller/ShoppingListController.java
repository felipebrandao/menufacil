package br.com.felipebrandao.menufacil.controller;

import br.com.felipebrandao.menufacil.dto.shopping.ShoppingListResponse;
import br.com.felipebrandao.menufacil.service.impl.ShoppingListServiceImpl;
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

    private final ShoppingListServiceImpl shoppingListService;

    @GetMapping
    public ResponseEntity<ShoppingListResponse> generate(
            @RequestParam String view,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month
    ) {
        return ResponseEntity.ok(shoppingListService.generate(view, start, year, month));
    }
}
