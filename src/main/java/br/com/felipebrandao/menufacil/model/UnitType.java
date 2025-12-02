package br.com.felipebrandao.menufacil.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "unit_types")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnitType {

    @Id
    private String id;

    @NotBlank(message = "nome é obrigatório")
    private String name;

    @NotBlank(message = "abreviação é obrigatória")
    private String abbreviation;

}
