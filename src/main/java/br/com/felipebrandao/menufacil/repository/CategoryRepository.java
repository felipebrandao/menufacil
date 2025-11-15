package br.com.felipebrandao.menufacil.repository;

import br.com.felipebrandao.menufacil.model.Category;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends MongoRepository<Category, String> {

}
