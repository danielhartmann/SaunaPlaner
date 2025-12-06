package com.thermaflow.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a daily schedule containing all infusion slots for a specific date.
 */
@Entity
@Table(name = "daily_schedules")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailySchedule {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    @Column(nullable = false, unique = true)
    private LocalDate date;
    
    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("startTime ASC")
    @Builder.Default
    private List<InfusionSlot> slots = new ArrayList<>();
    
    @Column
    private Boolean published = false; // Whether the schedule has been published to guests
    
    @Column(length = 1000)
    private String notes;
    
    /**
     * Helper method to add a slot to the schedule.
     */
    public void addSlot(InfusionSlot slot) {
        slots.add(slot);
        slot.setSchedule(this);
    }
    
    /**
     * Helper method to remove a slot from the schedule.
     */
    public void removeSlot(InfusionSlot slot) {
        slots.remove(slot);
        slot.setSchedule(null);
    }
}
