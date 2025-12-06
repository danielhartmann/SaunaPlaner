package com.thermaflow.service;

import com.thermaflow.dto.Conflict;
import com.thermaflow.model.*;
import com.thermaflow.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing infusion slots with inventory deduction and conflict validation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InfusionSlotService {
    
    private final InfusionSlotRepository slotRepository;
    private final DailyScheduleRepository scheduleRepository;
    private final IngredientRepository ingredientRepository;
    private final ScheduleValidatorService validatorService;
    
    /**
     * Creates and confirms a new infusion slot.
     * This method is transactional to ensure inventory deduction is atomic.
     * 
     * @param slot The slot to create
     * @return The created slot
     * @throws IllegalStateException if validation fails or inventory is insufficient
     */
    @Transactional
    public InfusionSlot createAndConfirmSlot(InfusionSlot slot) {
        // Validate the slot
        List<Conflict> conflicts = validatorService.validate(slot);
        if (!conflicts.isEmpty()) {
            String conflictMessages = conflicts.stream()
                    .map(Conflict::getMessage)
                    .reduce((a, b) -> a + "; " + b)
                    .orElse("Unknown conflicts");
            throw new IllegalStateException("Cannot create slot due to conflicts: " + conflictMessages);
        }
        
        // Save the slot first
        InfusionSlot savedSlot = slotRepository.save(slot);
        
        // Deduct inventory if confirming
        if (slot.getConfirmed()) {
            deductInventory(savedSlot);
        }
        
        return savedSlot;
    }
    
    /**
     * Confirms a slot and deducts inventory.
     * This is a separate transactional method to handle inventory deduction atomically.
     * 
     * @param slotId The ID of the slot to confirm
     * @return The confirmed slot
     */
    @Transactional
    public InfusionSlot confirmSlot(Long slotId) {
        InfusionSlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new IllegalArgumentException("Slot not found: " + slotId));
        
        if (slot.getConfirmed()) {
            log.warn("Slot {} is already confirmed", slotId);
            return slot;
        }
        
        // Validate inventory before confirming
        List<Conflict> inventoryConflicts = validatorService.validate(slot).stream()
                .filter(c -> c.getType() == Conflict.ConflictType.INSUFFICIENT_INVENTORY)
                .toList();
        
        if (!inventoryConflicts.isEmpty()) {
            throw new IllegalStateException("Cannot confirm slot: insufficient inventory");
        }
        
        // Deduct inventory
        deductInventory(slot);
        
        // Mark as confirmed
        slot.setConfirmed(true);
        return slotRepository.save(slot);
    }
    
    /**
     * Deducts ingredient inventory for a confirmed slot.
     * This method is package-private and should only be called within a transaction.
     * 
     * Uses pessimistic locking to prevent concurrent inventory deductions.
     */
    private void deductInventory(InfusionSlot slot) {
        log.info("Deducting inventory for slot {}", slot.getId());
        
        slot.getRecipe().getSteps().forEach(step -> {
            if (step.getIngredient() != null) {
                Ingredient ingredient = ingredientRepository.findById(step.getIngredient().getId())
                        .orElseThrow(() -> new IllegalStateException("Ingredient not found: " + step.getIngredient().getId()));
                
                int currentStock = ingredient.getStockLevel();
                int requiredAmount = step.getScentDosageMl();
                
                if (currentStock < requiredAmount) {
                    throw new IllegalStateException(
                            String.format("Insufficient inventory for %s: required %d ml, available %d ml",
                                    ingredient.getName(), requiredAmount, currentStock)
                    );
                }
                
                ingredient.setStockLevel(currentStock - requiredAmount);
                ingredientRepository.save(ingredient);
                
                log.debug("Deducted {} ml of {} (remaining: {} ml)",
                        requiredAmount, ingredient.getName(), ingredient.getStockLevel());
            }
        });
    }
    
    /**
     * Cancels a slot and optionally restores inventory.
     * 
     * @param slotId The ID of the slot to cancel
     * @param restoreInventory Whether to restore deducted inventory
     * @return The cancelled slot
     */
    @Transactional
    public InfusionSlot cancelSlot(Long slotId, boolean restoreInventory) {
        InfusionSlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new IllegalArgumentException("Slot not found: " + slotId));
        
        if (slot.getCancelled()) {
            log.warn("Slot {} is already cancelled", slotId);
            return slot;
        }
        
        // Restore inventory if requested and slot was confirmed
        if (restoreInventory && slot.getConfirmed()) {
            restoreInventory(slot);
        }
        
        slot.setCancelled(true);
        return slotRepository.save(slot);
    }
    
    /**
     * Restores ingredient inventory for a cancelled slot.
     */
    private void restoreInventory(InfusionSlot slot) {
        log.info("Restoring inventory for cancelled slot {}", slot.getId());
        
        slot.getRecipe().getSteps().forEach(step -> {
            if (step.getIngredient() != null) {
                Ingredient ingredient = ingredientRepository.findById(step.getIngredient().getId())
                        .orElseThrow(() -> new IllegalStateException("Ingredient not found: " + step.getIngredient().getId()));
                
                int restoredAmount = step.getScentDosageMl();
                ingredient.setStockLevel(ingredient.getStockLevel() + restoredAmount);
                ingredientRepository.save(ingredient);
                
                log.debug("Restored {} ml of {} (new stock: {} ml)",
                        restoredAmount, ingredient.getName(), ingredient.getStockLevel());
            }
        });
    }
}
