package com.thermaflow.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing an infusion recipe.
 * A recipe contains multiple steps and calculates total cost and duration dynamically.
 */
@Entity
@Table(name = "infusion_recipes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InfusionRecipe {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Column(nullable = false)
    private String name;
    
    @Column(length = 2000)
    private String description;
    
    @Column(length = 500)
    private String theme; // e.g., "Nordic Aurora", "Tropical Paradise"
    
    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("stepOrder ASC")
    @Builder.Default
    private List<InfusionStep> steps = new ArrayList<>();
    
    /**
     * Calculates the total duration of the recipe by summing all step durations.
     * 
     * @return Total duration in seconds
     */
    public Integer calculateTotalDuration() {
        return steps.stream()
                .mapToInt(InfusionStep::getDurationSeconds)
                .sum();
    }
    
    /**
     * Calculates the total cost of the recipe based on ingredient usage in each step.
     * 
     * @return Total cost as BigDecimal
     */
    public BigDecimal calculateTotalCost() {
        return steps.stream()
                .filter(step -> step.getIngredient() != null)
                .map(step -> step.getIngredient().getCostPerMl()
                        .multiply(BigDecimal.valueOf(step.getScentDosageMl())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * Helper method to add a step to the recipe.
     */
    public void addStep(InfusionStep step) {
        steps.add(step);
        step.setRecipe(this);
        step.setStepOrder(steps.size() - 1);
    }
    
    /**
     * Helper method to remove a step from the recipe.
     */
    public void removeStep(InfusionStep step) {
        steps.remove(step);
        step.setRecipe(null);
        // Reorder remaining steps
        for (int i = 0; i < steps.size(); i++) {
            steps.get(i).setStepOrder(i);
        }
    }
}
