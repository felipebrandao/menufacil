package br.com.felipebrandao.menufacil.repository;

import br.com.felipebrandao.menufacil.model.Recipe;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipeRepository extends MongoRepository<Recipe, String> {
    List<Recipe> findByNameContainingIgnoreCase(String name);
    List<Recipe> findByCategoryId(String category);
}
