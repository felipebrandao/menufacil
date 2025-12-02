package br.com.felipebrandao.menufacil.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnitConversion {

    private UnitType toUnit;
    private Double factor;
}
