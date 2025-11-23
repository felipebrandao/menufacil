package br.com.felipebrandao.menufacil.repository;

import br.com.felipebrandao.menufacil.model.RecipeCategory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RecipeCategoryRepository extends MongoRepository<RecipeCategory, String> {
    boolean existsByName(String name);
    Optional<RecipeCategory> findByName(String name);
}
