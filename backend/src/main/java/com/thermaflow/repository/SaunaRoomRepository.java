package com.thermaflow.repository;

import com.thermaflow.model.SaunaRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for SaunaRoom entity.
 */
@Repository
public interface SaunaRoomRepository extends JpaRepository<SaunaRoom, Long> {
    Optional<SaunaRoom> findByName(String name);
}
