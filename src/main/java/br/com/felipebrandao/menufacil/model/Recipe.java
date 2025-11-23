package br.com.felipebrandao.menufacil.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Document(collection = "recipes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Recipe {

    @Id
    private String id = UUID.randomUUID().toString();

    @NotBlank
    private String name;

    @NotNull
    private RecipeCategory category;

    @Size(min = 1, message = "A receita deve ter pelo menos um ingrediente.")
    private List<RecipeIngredient> ingredients;

    private List<String> instructions;

    private String mainImage;

    private List<String> gallery;

    private Integer totalTime;

    @Builder.Default
    private Boolean highlighted = false;

    @CreatedDate
    private Instant createdAt;

    private Instant updatedAt;
}
