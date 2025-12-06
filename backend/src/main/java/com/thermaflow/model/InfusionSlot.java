package com.thermaflow.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Entity representing a scheduled infusion slot.
 * Connects a sauna room, recipe, employee, and start time.
 */
@Entity
@Table(name = "infusion_slots")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InfusionSlot {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private DailySchedule schedule;
    
    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "room_id", nullable = false)
    private SaunaRoom room;
    
    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "recipe_id", nullable = false)
    private InfusionRecipe recipe;
    
    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
    
    @NotNull
    @Column(nullable = false)
    private LocalTime startTime;
    
    @Column
    private Boolean confirmed = false; // Whether ingredients have been deducted
    
    @Column
    private Boolean cancelled = false;
    
    @Column(length = 500)
    private String notes;
    
    /**
     * Calculate the end time of this slot based on the recipe duration.
     */
    public LocalTime getEndTime() {
        return startTime.plusSeconds(recipe.calculateTotalDuration());
    }
    
    /**
     * Calculate the end time including the room's cool-down period.
     */
    public LocalTime getEndTimeWithCoolDown() {
        return getEndTime().plusMinutes(room.getRequiredCoolDownMin());
    }
    
    /**
     * Get the average heat intensity of the recipe.
     */
    public double getAverageHeatIntensity() {
        return recipe.getSteps().stream()
                .mapToInt(InfusionStep::getHeatIntensity)
                .average()
                .orElse(0.0);
    }
}
