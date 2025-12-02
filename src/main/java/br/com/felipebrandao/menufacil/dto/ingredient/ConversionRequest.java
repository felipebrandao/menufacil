package br.com.felipebrandao.menufacil.dto.ingredient;

import lombok.Data;

@Data
public class ConversionRequest {

    private String toUnit;

    private Double factor;
}
