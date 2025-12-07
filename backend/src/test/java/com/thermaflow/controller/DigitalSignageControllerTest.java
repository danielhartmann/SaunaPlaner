package com.thermaflow.controller;

import com.thermaflow.dto.SignageDisplayDTO;
import com.thermaflow.service.DigitalSignageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DigitalSignageController.class)
class DigitalSignageControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private DigitalSignageService signageService;
    
    private SignageDisplayDTO testDisplay;
    
    @BeforeEach
    void setUp() {
        testDisplay = SignageDisplayDTO.builder()
                .roomName("Finnish Sauna")
                .recipeName("Nordic Aurora")
                .startTime("14:00")
                .endTime("14:20")
                .duration("20 min")
                .intensity("Mittel")
                .intensityIcon("🔥🔥")
                .scentProfiles(List.of("HERBAL", "WOODY"))
                .theme("Northern Lights Experience")
                .isCurrentlyRunning(false)
                .build();
    }
    
    @Test
    void testGetTodaySchedule() throws Exception {
        // Arrange
        when(signageService.getTodaySchedule())
                .thenReturn(Arrays.asList(testDisplay));
        
        // Act & Assert
        mockMvc.perform(get("/api/signage/today")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].roomName", is("Finnish Sauna")))
                .andExpect(jsonPath("$[0].recipeName", is("Nordic Aurora")))
                .andExpect(jsonPath("$[0].startTime", is("14:00")))
                .andExpect(jsonPath("$[0].endTime", is("14:20")))
                .andExpect(jsonPath("$[0].duration", is("20 min")))
                .andExpect(jsonPath("$[0].intensity", is("Mittel")))
                .andExpect(jsonPath("$[0].intensityIcon", is("🔥🔥")))
                .andExpect(jsonPath("$[0].theme", is("Northern Lights Experience")))
                .andExpect(jsonPath("$[0].isCurrentlyRunning", is(false)));
    }
    
    @Test
    void testGetScheduleByDate() throws Exception {
        // Arrange
        LocalDate testDate = LocalDate.of(2025, 12, 25);
        when(signageService.getScheduleForDate(testDate))
                .thenReturn(Arrays.asList(testDisplay));
        
        // Act & Assert
        mockMvc.perform(get("/api/signage/2025-12-25")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].roomName", is("Finnish Sauna")));
    }
    
    @Test
    void testGetNextInfusions() throws Exception {
        // Arrange
        SignageDisplayDTO next1 = SignageDisplayDTO.builder()
                .roomName("Finnish Sauna")
                .recipeName("Nordic Aurora")
                .startTime("15:00")
                .endTime("15:20")
                .duration("20 min")
                .intensity("Mittel")
                .intensityIcon("🔥🔥")
                .build();
        
        SignageDisplayDTO next2 = SignageDisplayDTO.builder()
                .roomName("Bio Sauna")
                .recipeName("Tropical Paradise")
                .startTime("16:00")
                .endTime("16:15")
                .duration("15 min")
                .intensity("Mild")
                .intensityIcon("🔥")
                .build();
        
        when(signageService.getNextInfusions())
                .thenReturn(Arrays.asList(next1, next2));
        
        // Act & Assert
        mockMvc.perform(get("/api/signage/next")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].startTime", is("15:00")))
                .andExpect(jsonPath("$[1].startTime", is("16:00")));
    }
    
    @Test
    void testGetCurrentInfusion_Found() throws Exception {
        // Arrange
        testDisplay.setIsCurrentlyRunning(true);
        when(signageService.getCurrentInfusion())
                .thenReturn(Optional.of(testDisplay));
        
        // Act & Assert
        mockMvc.perform(get("/api/signage/current")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.roomName", is("Finnish Sauna")))
                .andExpect(jsonPath("$.isCurrentlyRunning", is(true)));
    }
    
    @Test
    void testGetCurrentInfusion_NotFound() throws Exception {
        // Arrange
        when(signageService.getCurrentInfusion())
                .thenReturn(Optional.empty());
        
        // Act & Assert
        mockMvc.perform(get("/api/signage/current")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
    
    @Test
    void testGetTodaySchedule_EmptyList() throws Exception {
        // Arrange
        when(signageService.getTodaySchedule())
                .thenReturn(List.of());
        
        // Act & Assert
        mockMvc.perform(get("/api/signage/today")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }
    
    @Test
    void testIntensityLevels() throws Exception {
        // Arrange - Test all three intensity levels
        SignageDisplayDTO mild = SignageDisplayDTO.builder()
                .roomName("Bio Sauna")
                .recipeName("Gentle Breeze")
                .startTime("10:00")
                .endTime("10:15")
                .duration("15 min")
                .intensity("Mild")
                .intensityIcon("🔥")
                .build();
        
        SignageDisplayDTO mittel = SignageDisplayDTO.builder()
                .roomName("Finnish Sauna")
                .recipeName("Classic Session")
                .startTime("12:00")
                .endTime("12:20")
                .duration("20 min")
                .intensity("Mittel")
                .intensityIcon("🔥🔥")
                .build();
        
        SignageDisplayDTO intensiv = SignageDisplayDTO.builder()
                .roomName("Kelo Sauna")
                .recipeName("Fire Storm")
                .startTime("14:00")
                .endTime("14:25")
                .duration("25 min")
                .intensity("Intensiv")
                .intensityIcon("🔥🔥🔥")
                .build();
        
        when(signageService.getTodaySchedule())
                .thenReturn(Arrays.asList(mild, mittel, intensiv));
        
        // Act & Assert
        mockMvc.perform(get("/api/signage/today")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].intensity", is("Mild")))
                .andExpect(jsonPath("$[0].intensityIcon", is("🔥")))
                .andExpect(jsonPath("$[1].intensity", is("Mittel")))
                .andExpect(jsonPath("$[1].intensityIcon", is("🔥🔥")))
                .andExpect(jsonPath("$[2].intensity", is("Intensiv")))
                .andExpect(jsonPath("$[2].intensityIcon", is("🔥🔥🔥")));
    }
}
