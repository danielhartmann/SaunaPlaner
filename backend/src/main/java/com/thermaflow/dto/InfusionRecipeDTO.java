package com.thermaflow.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for InfusionRecipe.
 */
@Data
public class InfusionRecipeDTO {
    private Long id;
    
    @NotBlank
    private String name;
    
    private String description;
    private String theme;
    private List<InfusionStepDTO> steps;
    
    // Calculated fields
    private Integer totalDuration;
    private BigDecimal totalCost;
}
