package com.thermaflow.service;

import com.thermaflow.dto.Conflict;
import com.thermaflow.model.*;
import com.thermaflow.repository.IngredientRepository;
import com.thermaflow.repository.InfusionSlotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Tests for ScheduleValidatorService
 */
@ExtendWith(MockitoExtension.class)
class ScheduleValidatorServiceTest {
    
    @Mock
    private InfusionSlotRepository slotRepository;
    
    @Mock
    private IngredientRepository ingredientRepository;
    
    @InjectMocks
    private ScheduleValidatorService validatorService;
    
    private DailySchedule schedule;
    private SaunaRoom room;
    private Employee employee;
    private InfusionRecipe recipe;
    private Ingredient ingredient;
    
    @BeforeEach
    void setUp() {
        schedule = DailySchedule.builder()
                .id(1L)
                .date(LocalDate.now())
                .build();
        
        room = SaunaRoom.builder()
                .id(1L)
                .name("Test Room")
                .capacity(10)
                .type(SaunaType.FINNISH)
                .hasSoundSystem(true)
                .requiredCoolDownMin(15)
                .build();
        
        employee = Employee.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .certificationLevel(4)
                .dailyMaxInfusions(6)
                .build();
        
        ingredient = Ingredient.builder()
                .id(1L)
                .name("Test Ingredient")
                .viscosity(20)
                .scentProfile(ScentProfile.HERBAL)
                .stockLevel(1000)
                .costPerMl(BigDecimal.valueOf(0.15))
                .build();
        
        InfusionStep step = InfusionStep.builder()
                .id(1L)
                .name("Round 1")
                .durationSeconds(300)
                .heatIntensity(5)
                .scentDosageMl(50)
                .ingredient(ingredient)
                .stepOrder(0)
                .build();
        
        recipe = InfusionRecipe.builder()
                .id(1L)
                .name("Test Recipe")
                .steps(new ArrayList<>(List.of(step)))
                .build();
        
        step.setRecipe(recipe);
    }
    
    @Test
    void testValidateNoConflicts() {
        InfusionSlot newSlot = InfusionSlot.builder()
                .schedule(schedule)
                .room(room)
                .recipe(recipe)
                .employee(employee)
                .startTime(LocalTime.of(10, 0))
                .build();
        
        when(slotRepository.findByScheduleDateAndNotCancelled(any())).thenReturn(List.of());
        
        List<Conflict> conflicts = validatorService.validate(newSlot);
        
        assertTrue(conflicts.isEmpty(), "Should have no conflicts");
    }
    
    @Test
    void testValidateEmployeeConflict() {
        InfusionSlot existingSlot = InfusionSlot.builder()
                .id(1L)
                .schedule(schedule)
                .room(room)
                .recipe(recipe)
                .employee(employee)
                .startTime(LocalTime.of(10, 0))
                .build();
        
        InfusionSlot newSlot = InfusionSlot.builder()
                .schedule(schedule)
                .room(room)
                .recipe(recipe)
                .employee(employee)
                .startTime(LocalTime.of(10, 2)) // Overlaps with existing slot
                .build();
        
        when(slotRepository.findByScheduleDateAndNotCancelled(any()))
                .thenReturn(List.of(existingSlot));
        
        List<Conflict> conflicts = validatorService.validate(newSlot);
        
        assertFalse(conflicts.isEmpty(), "Should have employee conflict");
        assertEquals(Conflict.ConflictType.EMPLOYEE_UNAVAILABLE, conflicts.get(0).getType());
    }
    
    @Test
    void testValidateRoomCooldownConflict() {
        // Create existing slot ending at 10:05 (300 seconds from 10:00)
        InfusionSlot existingSlot = InfusionSlot.builder()
                .id(1L)
                .schedule(schedule)
                .room(room)
                .recipe(recipe)
                .employee(employee)
                .startTime(LocalTime.of(10, 0))
                .build();
        
        // New slot starts at 10:10, but room needs 15 min cooldown until 10:20
        InfusionSlot newSlot = InfusionSlot.builder()
                .schedule(schedule)
                .room(room)
                .recipe(recipe)
                .employee(Employee.builder().id(2L).firstName("Jane").lastName("Doe").build())
                .startTime(LocalTime.of(10, 10))
                .build();
        
        when(slotRepository.findByScheduleDateAndNotCancelled(any()))
                .thenReturn(List.of(existingSlot));
        
        List<Conflict> conflicts = validatorService.validate(newSlot);
        
        assertFalse(conflicts.isEmpty(), "Should have cooldown conflict");
        assertTrue(conflicts.stream().anyMatch(c -> 
                c.getType() == Conflict.ConflictType.ROOM_COOLDOWN_VIOLATION));
    }
}
