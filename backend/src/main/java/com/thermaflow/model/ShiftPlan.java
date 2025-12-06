package com.thermaflow.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Entity representing an employee's shift plan for a specific date.
 */
@Entity
@Table(name = "shift_plans")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShiftPlan {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
    
    @NotNull
    @Column(nullable = false)
    private LocalDate date;
    
    @NotNull
    @Column(nullable = false)
    private LocalTime startTime;
    
    @NotNull
    @Column(nullable = false)
    private LocalTime endTime;
    
    @Column(length = 500)
    private String notes;
}
