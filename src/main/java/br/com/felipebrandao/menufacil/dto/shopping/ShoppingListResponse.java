package br.com.felipebrandao.menufacil.dto.shopping;

import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class ShoppingListResponse {

    private String view;
    private LocalDate start;
    private LocalDate end;

    private List<ShoppingListRecipeResponse> recipes = new ArrayList<>();
    private List<ShoppingListCategoryResponse> categories = new ArrayList<>();
}
