package com.thermaflow.service;

import com.thermaflow.dto.Conflict;
import com.thermaflow.model.Employee;
import com.thermaflow.model.InfusionSlot;
import com.thermaflow.model.InfusionStep;
import com.thermaflow.repository.IngredientRepository;
import com.thermaflow.repository.InfusionSlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for validating infusion slot schedules.
 * Checks for conflicts including staff availability, room availability, and cool-down rules.
 */
@Service
@RequiredArgsConstructor
public class ScheduleValidatorService {
    
    private final InfusionSlotRepository slotRepository;
    private final IngredientRepository ingredientRepository;
    
    /**
     * Validates a new infusion slot and returns a list of conflicts.
     * Uses Java Streams for efficient conflict detection.
     * 
     * @param newSlot The slot to validate
     * @return List of conflicts found (empty if no conflicts)
     */
    public List<Conflict> validate(InfusionSlot newSlot) {
        List<Conflict> conflicts = new ArrayList<>();
        
        // Get existing slots for the same date
        List<InfusionSlot> existingSlots = slotRepository.findByScheduleDateAndNotCancelled(
                newSlot.getSchedule().getDate()
        );
        
        // Calculate new slot time boundaries
        LocalTime newSlotStart = newSlot.getStartTime();
        LocalTime newSlotEnd = newSlot.getEndTime();
        
        // Check employee availability
        conflicts.addAll(validateEmployeeAvailability(newSlot, existingSlots, newSlotStart, newSlotEnd));
        
        // Check room availability with cool-down
        conflicts.addAll(validateRoomAvailability(newSlot, existingSlots, newSlotStart, newSlotEnd));
        
        // Check inventory
        conflicts.addAll(validateInventory(newSlot));
        
        return conflicts;
    }
    
    /**
     * Validates employee availability and constraints.
     */
    private List<Conflict> validateEmployeeAvailability(
            InfusionSlot newSlot, 
            List<InfusionSlot> existingSlots,
            LocalTime newSlotStart,
            LocalTime newSlotEnd) {
        
        return existingSlots.stream()
                .filter(slot -> slot.getEmployee().getId().equals(newSlot.getEmployee().getId()))
                .filter(slot -> timesOverlap(slot.getStartTime(), slot.getEndTime(), newSlotStart, newSlotEnd))
                .map(slot -> new Conflict(
                        Conflict.ConflictType.EMPLOYEE_UNAVAILABLE,
                        String.format("Employee %s is already scheduled from %s to %s",
                                slot.getEmployee().getFullName(),
                                slot.getStartTime(),
                                slot.getEndTime()),
                        slot.getId(),
                        slot.getEmployee().getFullName()
                ))
                .collect(Collectors.toList());
    }
    
    /**
     * Validates room availability including cool-down periods.
     */
    private List<Conflict> validateRoomAvailability(
            InfusionSlot newSlot,
            List<InfusionSlot> existingSlots,
            LocalTime newSlotStart,
            LocalTime newSlotEnd) {
        
        List<Conflict> conflicts = new ArrayList<>();
        
        // Calculate new slot end time with cool-down
        LocalTime newSlotEndWithCoolDown = newSlot.getEndTimeWithCoolDown();
        
        // Find room conflicts
        existingSlots.stream()
                .filter(slot -> slot.getRoom().getId().equals(newSlot.getRoom().getId()))
                .forEach(slot -> {
                    LocalTime existingStart = slot.getStartTime();
                    LocalTime existingEnd = slot.getEndTime();
                    LocalTime existingEndWithCoolDown = slot.getEndTimeWithCoolDown();
                    
                    // Check if new slot overlaps with existing slot (including cool-down)
                    if (timesOverlap(existingStart, existingEndWithCoolDown, newSlotStart, newSlotEndWithCoolDown)) {
                        // Determine conflict type
                        if (timesOverlap(existingStart, existingEnd, newSlotStart, newSlotEnd)) {
                            // Direct overlap
                            conflicts.add(new Conflict(
                                    Conflict.ConflictType.ROOM_OCCUPIED,
                                    String.format("Room %s is occupied from %s to %s",
                                            slot.getRoom().getName(),
                                            existingStart,
                                            existingEnd),
                                    slot.getId(),
                                    slot.getRoom().getName()
                            ));
                        } else {
                            // Cool-down violation
                            conflicts.add(new Conflict(
                                    Conflict.ConflictType.ROOM_COOLDOWN_VIOLATION,
                                    String.format("Room %s requires cool-down until %s (previous session ends at %s, %d min cool-down required)",
                                            slot.getRoom().getName(),
                                            existingEndWithCoolDown,
                                            existingEnd,
                                            slot.getRoom().getRequiredCoolDownMin()),
                                    slot.getId(),
                                    slot.getRoom().getName()
                            ));
                        }
                    }
                });
        
        return conflicts;
    }
    
    /**
     * Validates ingredient inventory levels.
     */
    private List<Conflict> validateInventory(InfusionSlot newSlot) {
        List<Conflict> conflicts = new ArrayList<>();
        
        newSlot.getRecipe().getSteps().stream()
                .filter(step -> step.getIngredient() != null)
                .collect(Collectors.groupingBy(
                        InfusionStep::getIngredient,
                        Collectors.summingInt(InfusionStep::getScentDosageMl)
                ))
                .forEach((ingredient, requiredAmount) -> {
                    if (ingredient.getStockLevel() < requiredAmount) {
                        conflicts.add(new Conflict(
                                Conflict.ConflictType.INSUFFICIENT_INVENTORY,
                                String.format("Insufficient inventory for ingredient %s: required %d ml, available %d ml",
                                        ingredient.getName(),
                                        requiredAmount,
                                        ingredient.getStockLevel()),
                                null,
                                ingredient.getName()
                        ));
                    }
                });
        
        return conflicts;
    }
    
    /**
     * Checks if employee exceeds daily maximum infusions.
     */
    public List<Conflict> checkEmployeeHealthConstraints(Employee employee, List<InfusionSlot> dailySlots) {
        long employeeSlotCount = dailySlots.stream()
                .filter(slot -> slot.getEmployee().getId().equals(employee.getId()))
                .count();
        
        if (employeeSlotCount >= employee.getDailyMaxInfusions()) {
            return List.of(new Conflict(
                    Conflict.ConflictType.EMPLOYEE_MAX_INFUSIONS_EXCEEDED,
                    String.format("Employee %s has reached or exceeded daily maximum of %d infusions (current: %d)",
                            employee.getFullName(),
                            employee.getDailyMaxInfusions(),
                            employeeSlotCount),
                    null,
                    employee.getFullName()
            ));
        }
        
        return List.of();
    }
    
    /**
     * Helper method to check if two time ranges overlap.
     * Uses proper interval comparison: two intervals overlap if 
     * start1 < end2 AND start2 < end1
     */
    private boolean timesOverlap(LocalTime start1, LocalTime end1, LocalTime start2, LocalTime end2) {
        // Validate inputs
        if (start1 == null || end1 == null || start2 == null || end2 == null) {
            throw new IllegalArgumentException("Time values cannot be null");
        }
        
        // Two intervals overlap if: start1 < end2 AND start2 < end1
        return start1.isBefore(end2) && start2.isBefore(end1);
    }
}
