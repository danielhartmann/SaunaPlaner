package com.thermaflow.service;

import com.thermaflow.dto.SignageDisplayDTO;
import com.thermaflow.model.*;
import com.thermaflow.repository.InfusionSlotRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DigitalSignageServiceTest {
    
    @Mock
    private InfusionSlotRepository slotRepository;
    
    @InjectMocks
    private DigitalSignageService signageService;
    
    private InfusionSlot testSlot;
    private SaunaRoom testRoom;
    private InfusionRecipe testRecipe;
    private Employee testEmployee;
    private Ingredient testIngredient;
    
    @BeforeEach
    void setUp() {
        // Create test room
        testRoom = SaunaRoom.builder()
                .id(1L)
                .name("Finnish Sauna")
                .capacity(12)
                .type(SaunaType.FINNISH)
                .hasSoundSystem(true)
                .requiredCoolDownMin(15)
                .build();
        
        // Create test ingredient with scent profile
        testIngredient = Ingredient.builder()
                .id(1L)
                .name("Eucalyptus Oil")
                .scentProfile(ScentProfile.HERBAL)
                .build();
        
        // Create test recipe with steps
        testRecipe = InfusionRecipe.builder()
                .id(1L)
                .name("Nordic Aurora")
                .theme("Northern Lights Experience")
                .description("A journey through the arctic night")
                .steps(new ArrayList<>())
                .build();
        
        // Add steps to recipe
        InfusionStep step1 = InfusionStep.builder()
                .id(1L)
                .name("Warm-up")
                .durationSeconds(300) // 5 minutes
                .heatIntensity(3)
                .scentDosageMl(10)
                .ingredient(testIngredient)
                .recipe(testRecipe)
                .stepOrder(0)
                .build();
        
        InfusionStep step2 = InfusionStep.builder()
                .id(2L)
                .name("Main Heat")
                .durationSeconds(600) // 10 minutes
                .heatIntensity(8)
                .scentDosageMl(15)
                .ingredient(testIngredient)
                .recipe(testRecipe)
                .stepOrder(1)
                .build();
        
        InfusionStep step3 = InfusionStep.builder()
                .id(3L)
                .name("Cool-down")
                .durationSeconds(300) // 5 minutes
                .heatIntensity(2)
                .scentDosageMl(5)
                .ingredient(testIngredient)
                .recipe(testRecipe)
                .stepOrder(2)
                .build();
        
        testRecipe.getSteps().addAll(Arrays.asList(step1, step2, step3));
        
        // Create test employee
        testEmployee = Employee.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .build();
        
        // Create test schedule
        DailySchedule schedule = DailySchedule.builder()
                .id(1L)
                .date(LocalDate.now())
                .build();
        
        // Create test slot
        testSlot = InfusionSlot.builder()
                .id(1L)
                .schedule(schedule)
                .room(testRoom)
                .recipe(testRecipe)
                .employee(testEmployee)
                .startTime(LocalTime.of(14, 0))
                .confirmed(true)
                .cancelled(false)
                .build();
    }
    
    @Test
    void testGetTodaySchedule() {
        // Arrange
        when(slotRepository.findByScheduleDateAndNotCancelled(any(LocalDate.class)))
                .thenReturn(List.of(testSlot));
        
        // Act
        List<SignageDisplayDTO> result = signageService.getTodaySchedule();
        
        // Assert
        assertThat(result).hasSize(1);
        SignageDisplayDTO dto = result.get(0);
        assertThat(dto.getRoomName()).isEqualTo("Finnish Sauna");
        assertThat(dto.getRecipeName()).isEqualTo("Nordic Aurora");
        assertThat(dto.getTheme()).isEqualTo("Northern Lights Experience");
    }
    
    @Test
    void testGetScheduleForDate() {
        // Arrange
        LocalDate specificDate = LocalDate.of(2025, 12, 25);
        when(slotRepository.findByScheduleDateAndNotCancelled(specificDate))
                .thenReturn(List.of(testSlot));
        
        // Act
        List<SignageDisplayDTO> result = signageService.getScheduleForDate(specificDate);
        
        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRoomName()).isEqualTo("Finnish Sauna");
    }
    
    @Test
    void testIntensityMapping_Mild() {
        // Arrange - average intensity = (3 + 8 + 2) / 3 = 4.33 (should be Mittel)
        // Let's create a mild recipe
        InfusionRecipe mildRecipe = InfusionRecipe.builder()
                .id(2L)
                .name("Gentle Breeze")
                .theme("Relaxation")
                .steps(new ArrayList<>())
                .build();
        
        InfusionStep mildStep = InfusionStep.builder()
                .id(10L)
                .name("Gentle")
                .durationSeconds(600)
                .heatIntensity(2)
                .scentDosageMl(10)
                .ingredient(testIngredient)
                .recipe(mildRecipe)
                .stepOrder(0)
                .build();
        
        mildRecipe.getSteps().add(mildStep);
        
        InfusionSlot mildSlot = InfusionSlot.builder()
                .id(2L)
                .schedule(testSlot.getSchedule())
                .room(testRoom)
                .recipe(mildRecipe)
                .employee(testEmployee)
                .startTime(LocalTime.of(10, 0))
                .confirmed(true)
                .cancelled(false)
                .build();
        
        when(slotRepository.findByScheduleDateAndNotCancelled(any(LocalDate.class)))
                .thenReturn(List.of(mildSlot));
        
        // Act
        List<SignageDisplayDTO> result = signageService.getTodaySchedule();
        
        // Assert
        assertThat(result).hasSize(1);
        SignageDisplayDTO dto = result.get(0);
        assertThat(dto.getIntensity()).isEqualTo("Mild");
        assertThat(dto.getIntensityIcon()).isEqualTo("🔥");
    }
    
    @Test
    void testIntensityMapping_Mittel() {
        // testSlot has average intensity of (3 + 8 + 2) / 3 = 4.33
        // This should map to Mittel
        when(slotRepository.findByScheduleDateAndNotCancelled(any(LocalDate.class)))
                .thenReturn(List.of(testSlot));
        
        // Act
        List<SignageDisplayDTO> result = signageService.getTodaySchedule();
        
        // Assert
        assertThat(result).hasSize(1);
        SignageDisplayDTO dto = result.get(0);
        assertThat(dto.getIntensity()).isEqualTo("Mittel");
        assertThat(dto.getIntensityIcon()).isEqualTo("🔥🔥");
    }
    
    @Test
    void testIntensityMapping_Intensiv() {
        // Arrange - Create a high intensity recipe
        InfusionRecipe intensiveRecipe = InfusionRecipe.builder()
                .id(3L)
                .name("Fire Storm")
                .theme("Extreme Heat")
                .steps(new ArrayList<>())
                .build();
        
        InfusionStep intenseStep = InfusionStep.builder()
                .id(11L)
                .name("Maximum Heat")
                .durationSeconds(600)
                .heatIntensity(9)
                .scentDosageMl(20)
                .ingredient(testIngredient)
                .recipe(intensiveRecipe)
                .stepOrder(0)
                .build();
        
        intensiveRecipe.getSteps().add(intenseStep);
        
        InfusionSlot intenseSlot = InfusionSlot.builder()
                .id(3L)
                .schedule(testSlot.getSchedule())
                .room(testRoom)
                .recipe(intensiveRecipe)
                .employee(testEmployee)
                .startTime(LocalTime.of(16, 0))
                .confirmed(true)
                .cancelled(false)
                .build();
        
        when(slotRepository.findByScheduleDateAndNotCancelled(any(LocalDate.class)))
                .thenReturn(List.of(intenseSlot));
        
        // Act
        List<SignageDisplayDTO> result = signageService.getTodaySchedule();
        
        // Assert
        assertThat(result).hasSize(1);
        SignageDisplayDTO dto = result.get(0);
        assertThat(dto.getIntensity()).isEqualTo("Intensiv");
        assertThat(dto.getIntensityIcon()).isEqualTo("🔥🔥🔥");
    }
    
    @Test
    void testTimeFormatting() {
        // Arrange
        when(slotRepository.findByScheduleDateAndNotCancelled(any(LocalDate.class)))
                .thenReturn(List.of(testSlot));
        
        // Act
        List<SignageDisplayDTO> result = signageService.getTodaySchedule();
        
        // Assert
        assertThat(result).hasSize(1);
        SignageDisplayDTO dto = result.get(0);
        assertThat(dto.getStartTime()).isEqualTo("14:00");
        // Total duration is 1200 seconds = 20 minutes
        assertThat(dto.getEndTime()).isEqualTo("14:20");
    }
    
    @Test
    void testDurationFormatting() {
        // Arrange
        when(slotRepository.findByScheduleDateAndNotCancelled(any(LocalDate.class)))
                .thenReturn(List.of(testSlot));
        
        // Act
        List<SignageDisplayDTO> result = signageService.getTodaySchedule();
        
        // Assert
        assertThat(result).hasSize(1);
        SignageDisplayDTO dto = result.get(0);
        // Total duration is 1200 seconds = 20 minutes
        assertThat(dto.getDuration()).isEqualTo("20 min");
    }
    
    @Test
    void testScentProfileExtraction() {
        // Arrange
        when(slotRepository.findByScheduleDateAndNotCancelled(any(LocalDate.class)))
                .thenReturn(List.of(testSlot));
        
        // Act
        List<SignageDisplayDTO> result = signageService.getTodaySchedule();
        
        // Assert
        assertThat(result).hasSize(1);
        SignageDisplayDTO dto = result.get(0);
        assertThat(dto.getScentProfiles()).containsExactly("HERBAL");
    }
    
    @Test
    void testGetCurrentInfusion_Running() {
        // Arrange - slot runs from 14:00 to 14:20
        // We'll test at 14:10
        testSlot.setStartTime(LocalTime.now().minusMinutes(10));
        when(slotRepository.findByScheduleDateAndNotCancelled(any(LocalDate.class)))
                .thenReturn(List.of(testSlot));
        
        // Act
        Optional<SignageDisplayDTO> result = signageService.getCurrentInfusion();
        
        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getIsCurrentlyRunning()).isTrue();
    }
    
    @Test
    void testGetCurrentInfusion_NotRunning() {
        // Arrange - slot will run in the future
        testSlot.setStartTime(LocalTime.now().plusHours(2));
        when(slotRepository.findByScheduleDateAndNotCancelled(any(LocalDate.class)))
                .thenReturn(List.of(testSlot));
        
        // Act
        Optional<SignageDisplayDTO> result = signageService.getCurrentInfusion();
        
        // Assert
        assertThat(result).isEmpty();
    }
    
    @Test
    void testGetNextInfusions() {
        // Arrange - create multiple upcoming slots
        InfusionSlot slot1 = InfusionSlot.builder()
                .id(1L)
                .schedule(testSlot.getSchedule())
                .room(testRoom)
                .recipe(testRecipe)
                .employee(testEmployee)
                .startTime(LocalTime.now().plusHours(1))
                .confirmed(true)
                .cancelled(false)
                .build();
        
        InfusionSlot slot2 = InfusionSlot.builder()
                .id(2L)
                .schedule(testSlot.getSchedule())
                .room(testRoom)
                .recipe(testRecipe)
                .employee(testEmployee)
                .startTime(LocalTime.now().plusHours(2))
                .confirmed(true)
                .cancelled(false)
                .build();
        
        InfusionSlot slot3 = InfusionSlot.builder()
                .id(3L)
                .schedule(testSlot.getSchedule())
                .room(testRoom)
                .recipe(testRecipe)
                .employee(testEmployee)
                .startTime(LocalTime.now().plusHours(3))
                .confirmed(true)
                .cancelled(false)
                .build();
        
        when(slotRepository.findByScheduleDateAndNotCancelled(any(LocalDate.class)))
                .thenReturn(Arrays.asList(slot1, slot2, slot3));
        
        // Act
        List<SignageDisplayDTO> result = signageService.getNextInfusions();
        
        // Assert
        assertThat(result).hasSize(3);
        // Check they are in chronological order
        assertThat(result.get(0).getStartTime()).isNotNull();
        assertThat(result.get(1).getStartTime()).isNotNull();
        assertThat(result.get(2).getStartTime()).isNotNull();
    }
}
