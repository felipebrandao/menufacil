package br.com.felipebrandao.menufacil.repository;

import br.com.felipebrandao.menufacil.model.CategoryIngredient;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CategoryIngredientRepository extends MongoRepository<CategoryIngredient, String> {
    boolean existsByName(String name);
    Optional<CategoryIngredient> findByName(String name);
}
