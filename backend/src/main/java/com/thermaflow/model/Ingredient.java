package com.thermaflow.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Entity representing an ingredient used in infusion recipes.
 * Contains attributes like viscosity, scent profile, stock level, and cost.
 */
@Entity
@Table(name = "ingredients")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ingredient {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Column(nullable = false, unique = true)
    private String name;
    
    @NotNull
    @Min(0)
    @Column(nullable = false)
    private Integer viscosity; // Viscosity level (0-100)
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScentProfile scentProfile;
    
    @NotNull
    @Min(0)
    @Column(nullable = false)
    private Integer stockLevel; // Stock level in ml
    
    @NotNull
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal costPerMl; // Cost per milliliter
    
    @Column(length = 1000)
    private String description;
}
