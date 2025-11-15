package br.com.felipebrandao.menufacil.repository;

import br.com.felipebrandao.menufacil.model.UnitType;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UnitTypeRepository extends MongoRepository<UnitType, String> {
    boolean existsByName(String name);
    Optional<UnitType> findByName(String name);
}
