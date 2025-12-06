package com.thermaflow.repository;

import com.thermaflow.model.InfusionRecipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for InfusionRecipe entity.
 */
@Repository
public interface InfusionRecipeRepository extends JpaRepository<InfusionRecipe, Long> {
    
    @Query("SELECT r FROM InfusionRecipe r LEFT JOIN FETCH r.steps")
    List<InfusionRecipe> findAllWithSteps();
    
    List<InfusionRecipe> findByNameContainingIgnoreCase(String name);
}
