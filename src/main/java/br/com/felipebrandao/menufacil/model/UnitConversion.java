package br.com.felipebrandao.menufacil.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.DBRef;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnitConversion {

    @DBRef
    private UnitType toUnit;

    private Double factor;
}
