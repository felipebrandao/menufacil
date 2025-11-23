package br.com.felipebrandao.menufacil.dto.recipe;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RecipeCategoryResponse {
    private String id;
    private String name;
}
