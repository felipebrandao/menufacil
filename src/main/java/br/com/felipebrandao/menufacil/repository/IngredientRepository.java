package br.com.felipebrandao.menufacil.repository;

import br.com.felipebrandao.menufacil.model.Ingredient;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IngredientRepository extends MongoRepository<Ingredient, String> {
    boolean existsByNameAndCategoryId(String name, String categoryId);
    List<Ingredient> findByNameContainingIgnoreCase(String name, Pageable pageable);
    List<Ingredient> findByNameContainingIgnoreCaseAndCategoryId(String name, String categoryId, Pageable pageable);
    List<Ingredient> findByCategoryId(String categoryId);
    boolean existsByNameAndCategoryIdAndIdNot(@NotBlank String name, @NotBlank String category, String id);
}
