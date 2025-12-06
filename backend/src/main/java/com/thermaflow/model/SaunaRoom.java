package com.thermaflow.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity representing a sauna room with its properties and capabilities.
 */
@Entity
@Table(name = "sauna_rooms")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaunaRoom {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Column(nullable = false, unique = true)
    private String name;
    
    @NotNull
    @Min(1)
    @Column(nullable = false)
    private Integer capacity; // Maximum number of guests
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SaunaType type;
    
    @NotNull
    @Column(nullable = false)
    private Boolean hasSoundSystem;
    
    @NotNull
    @Min(0)
    @Column(nullable = false)
    private Integer requiredCoolDownMin; // Minutes needed between sessions for ventilation
    
    @Column(length = 1000)
    private String description;
    
    @Column
    private String location; // e.g., "Ground Floor", "Wellness Area"
}
