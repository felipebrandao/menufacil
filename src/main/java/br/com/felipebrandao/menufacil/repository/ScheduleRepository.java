package br.com.felipebrandao.menufacil.repository;

import br.com.felipebrandao.menufacil.model.ScheduleDay;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleRepository extends MongoRepository<ScheduleDay, String> {
    Optional<ScheduleDay> findByDate(LocalDate date);

    @Query("{ 'date': { $gte: ?0, $lte: ?1 } }")
    List<ScheduleDay> findByDateRange(LocalDate start, LocalDate end);
}
