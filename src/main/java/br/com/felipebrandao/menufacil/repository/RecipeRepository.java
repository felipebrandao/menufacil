package br.com.felipebrandao.menufacil.repository;

import br.com.felipebrandao.menufacil.model.Recipe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipeRepository extends MongoRepository<Recipe, String> {
    List<Recipe> findByNameContainingIgnoreCase(String name);
    List<Recipe> findByCategoryId(String category);

    @Query("""
        {
          $and: [
            {
              $or: [
                { ?0: '' },
                { "name": { $regex: ?0, $options: 'i' } }
              ]
            },
            {
              $or: [
                { ?1: '' },
                { "category.name": { $regex: ?1, $options: 'i' } }
              ]
            }
          ]
        }
        """)
    Page<Recipe> search(String query, String category, Pageable pageable);

}
