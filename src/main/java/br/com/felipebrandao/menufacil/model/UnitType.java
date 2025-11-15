package br.com.felipebrandao.menufacil.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Document(collection = "unit_types")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UnitType {

    @Id
    private String id = UUID.randomUUID().toString();

    @NotBlank(message = "nome é obrigatório")
    @Indexed(unique = true)
    private String name;

    @NotBlank(message = "abreviação é obrigatória")
    private String abbreviation;

}
