package br.com.felipebrandao.menufacil.dto.shopping;

import lombok.Data;

@Data
public class ShoppingListItemResponse {

    private String ingredientId;
    private String ingredientName;

    private Double quantity;
    private String unitId;
    private String unitName;
    private String unitAbbreviation;
}

