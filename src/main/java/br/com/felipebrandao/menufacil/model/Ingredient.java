package br.com.felipebrandao.menufacil.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Document(collection = "ingredients")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ingredient {

    @Id
    private String id = UUID.randomUUID().toString();

    @NotBlank(message = "nome é obrigatório")
    private String name;

    @DBRef
    @NotNull(message = "categoria é obrigatória")
    private CategoryIngredient category;

    @DBRef
    @NotNull(message = "unidade padrão é obrigatória")
    private UnitType defaultUnit;

    @Builder.Default
    private List<UnitConversion> conversions = new ArrayList<>();

    @CreatedDate
    private Instant createdAt;
}
