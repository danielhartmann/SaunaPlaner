package com.thermaflow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing a scheduling conflict.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Conflict {
    
    public enum ConflictType {
        EMPLOYEE_UNAVAILABLE,
        EMPLOYEE_MAX_INFUSIONS_EXCEEDED,
        EMPLOYEE_SKILL_MISSING,
        ROOM_OCCUPIED,
        ROOM_COOLDOWN_VIOLATION,
        INSUFFICIENT_INVENTORY
    }
    
    private ConflictType type;
    private String message;
    private Long relatedSlotId;
    private String relatedResourceName;
}
