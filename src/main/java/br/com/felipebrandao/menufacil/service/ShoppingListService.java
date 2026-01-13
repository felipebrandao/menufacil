package br.com.felipebrandao.menufacil.service;

import br.com.felipebrandao.menufacil.dto.shopping.ShoppingListResponse;

import java.time.LocalDate;

public interface ShoppingListService {

    ShoppingListResponse generate(String view, LocalDate start, Integer year, Integer month);
}

