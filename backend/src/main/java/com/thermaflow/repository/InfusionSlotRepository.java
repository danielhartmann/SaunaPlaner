package com.thermaflow.repository;

import com.thermaflow.model.InfusionSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for InfusionSlot entity.
 */
@Repository
public interface InfusionSlotRepository extends JpaRepository<InfusionSlot, Long> {
    
    @Query("SELECT s FROM InfusionSlot s WHERE s.schedule.date = :date AND s.cancelled = false")
    List<InfusionSlot> findByScheduleDateAndNotCancelled(@Param("date") LocalDate date);
    
    @Query("SELECT s FROM InfusionSlot s WHERE s.employee.id = :employeeId AND s.schedule.date = :date AND s.cancelled = false")
    List<InfusionSlot> findByEmployeeAndDate(@Param("employeeId") Long employeeId, @Param("date") LocalDate date);
    
    @Query("SELECT s FROM InfusionSlot s WHERE s.room.id = :roomId AND s.schedule.date = :date AND s.cancelled = false")
    List<InfusionSlot> findByRoomAndDate(@Param("roomId") Long roomId, @Param("date") LocalDate date);
}
