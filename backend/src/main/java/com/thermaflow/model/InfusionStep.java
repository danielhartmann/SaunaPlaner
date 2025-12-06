package com.thermaflow.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity representing a single step in an infusion recipe.
 * Each step defines duration, heat intensity, scent dosage, music, and lighting.
 */
@Entity
@Table(name = "infusion_steps")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InfusionStep {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    @Column(nullable = false)
    private String name; // e.g., "Round 1", "Round 2"
    
    @NotNull
    @Min(0)
    @Column(nullable = false)
    private Integer durationSeconds; // Duration of this step in seconds
    
    @NotNull
    @Min(1)
    @Max(10)
    @Column(nullable = false)
    private Integer heatIntensity; // Heat intensity level (1-10)
    
    @NotNull
    @Min(0)
    @Column(nullable = false)
    private Integer scentDosageMl; // Amount of scent to use in ml
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id")
    private Ingredient ingredient; // The ingredient used in this step
    
    @Column
    private String musicTrackId; // Reference to music track
    
    @Column
    private String lightingScene; // DMX code for lighting
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", nullable = false)
    private InfusionRecipe recipe;
    
    @Column(nullable = false)
    private Integer stepOrder; // Order of this step in the recipe
}
