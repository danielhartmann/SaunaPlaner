package com.thermaflow.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing an employee who can perform infusion rituals.
 * Includes certification level, health constraints, and special skills.
 */
@Entity
@Table(name = "employees")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Employee {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Column(nullable = false)
    private String firstName;
    
    @NotBlank
    @Column(nullable = false)
    private String lastName;
    
    @Email
    @Column(unique = true)
    private String email;
    
    @NotNull
    @Min(1)
    @Column(nullable = false)
    private Integer certificationLevel; // Certification level (1-5)
    
    @NotNull
    @Min(1)
    @Column(nullable = false)
    private Integer dailyMaxInfusions; // Maximum number of infusions per day for health safety
    
    @ElementCollection(targetClass = EmployeeSkill.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "employee_skills", joinColumns = @JoinColumn(name = "employee_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "skill")
    @Builder.Default
    private Set<EmployeeSkill> skills = new HashSet<>();
    
    @Column
    private Boolean active = true;
    
    /**
     * Check if employee has a specific skill.
     */
    public boolean hasSkill(EmployeeSkill skill) {
        return skills.contains(skill);
    }
    
    /**
     * Get full name of the employee.
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }
}
