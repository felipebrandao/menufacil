package br.com.felipebrandao.menufacil.dto.shopping;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ShoppingListCategoryResponse {

    private String categoryId;
    private String categoryName;

    private List<ShoppingListItemResponse> items = new ArrayList<>();
}

