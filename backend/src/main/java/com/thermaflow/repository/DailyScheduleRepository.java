package com.thermaflow.repository;

import com.thermaflow.model.DailySchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Repository for DailySchedule entity.
 */
@Repository
public interface DailyScheduleRepository extends JpaRepository<DailySchedule, Long> {
    
    Optional<DailySchedule> findByDate(LocalDate date);
    
    @Query("SELECT s FROM DailySchedule s LEFT JOIN FETCH s.slots WHERE s.date = :date")
    Optional<DailySchedule> findByDateWithSlots(@Param("date") LocalDate date);
}
