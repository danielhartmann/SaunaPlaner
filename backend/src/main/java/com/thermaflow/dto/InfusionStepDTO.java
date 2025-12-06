package com.thermaflow.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO for InfusionStep.
 */
@Data
public class InfusionStepDTO {
    private Long id;
    
    @NotNull
    private String name;
    
    @NotNull
    @Min(0)
    private Integer durationSeconds;
    
    @NotNull
    @Min(1)
    @Max(10)
    private Integer heatIntensity;
    
    @NotNull
    @Min(0)
    private Integer scentDosageMl;
    
    private Long ingredientId;
    private String ingredientName;
    private String musicTrackId;
    private String lightingScene;
    private Integer stepOrder;
}
