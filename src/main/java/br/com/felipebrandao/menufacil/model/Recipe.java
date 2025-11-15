package br.com.felipebrandao.menufacil.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
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
    private String name;
    private List<String> instructions;
    private String categoryId;
    private String categoryName;
    private List<RecipeIngredient> ingredients;
    private String mainImage;
    private List<String> gallery;

    @CreatedDate
    private LocalDateTime createdAt;
}
