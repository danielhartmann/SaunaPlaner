package com.thermaflow.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalTime;

/**
 * DTO for InfusionSlot.
 */
@Data
public class InfusionSlotDTO {
    private Long id;
    
    @NotNull
    private Long scheduleId;
    
    @NotNull
    private Long roomId;
    
    private String roomName;
    
    @NotNull
    private Long recipeId;
    
    private String recipeName;
    private String recipeTheme;
    
    @NotNull
    private Long employeeId;
    
    private String employeeName;
    
    @NotNull
    private LocalTime startTime;
    
    private LocalTime endTime;
    private Boolean confirmed;
    private Boolean cancelled;
    private String notes;
    private Double averageHeatIntensity;
}
