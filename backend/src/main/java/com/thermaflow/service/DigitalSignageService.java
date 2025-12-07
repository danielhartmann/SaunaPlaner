package com.thermaflow.service;

import com.thermaflow.dto.SignageDisplayDTO;
import com.thermaflow.model.InfusionSlot;
import com.thermaflow.model.InfusionStep;
import com.thermaflow.model.ScentProfile;
import com.thermaflow.repository.InfusionSlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for formatting infusion data for digital signage displays.
 * Provides guest-friendly, simplified data formatting.
 */
@Service
@RequiredArgsConstructor
public class DigitalSignageService {
    
    private final InfusionSlotRepository slotRepository;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    
    /**
     * Get all infusions for today formatted for signage display.
     */
    public List<SignageDisplayDTO> getTodaySchedule() {
        return getScheduleForDate(LocalDate.now());
    }
    
    /**
     * Get all infusions for a specific date formatted for signage display.
     */
    public List<SignageDisplayDTO> getScheduleForDate(LocalDate date) {
        List<InfusionSlot> slots = slotRepository.findByScheduleDateAndNotCancelled(date);
        LocalTime now = LocalTime.now();
        
        return slots.stream()
                .map(slot -> toSignageDisplay(slot, now))
                .collect(Collectors.toList());
    }
    
    /**
     * Get the next 3-5 upcoming infusions.
     */
    public List<SignageDisplayDTO> getNextInfusions() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        
        List<InfusionSlot> todaySlots = slotRepository.findByScheduleDateAndNotCancelled(today);
        
        // Filter for upcoming slots and take first 5
        List<SignageDisplayDTO> upcomingToday = todaySlots.stream()
                .filter(slot -> slot.getStartTime().isAfter(now))
                .sorted((s1, s2) -> s1.getStartTime().compareTo(s2.getStartTime()))
                .limit(5)
                .map(slot -> toSignageDisplay(slot, now))
                .collect(Collectors.toList());
        
        // If we have fewer than 3 today, try tomorrow
        if (upcomingToday.size() < 3) {
            LocalDate tomorrow = today.plusDays(1);
            List<InfusionSlot> tomorrowSlots = slotRepository.findByScheduleDateAndNotCancelled(tomorrow);
            
            List<SignageDisplayDTO> tomorrowFirst = tomorrowSlots.stream()
                    .sorted((s1, s2) -> s1.getStartTime().compareTo(s2.getStartTime()))
                    .limit(5 - upcomingToday.size())
                    .map(slot -> toSignageDisplay(slot, LocalTime.MIN)) // Tomorrow, so not currently running
                    .collect(Collectors.toList());
            
            upcomingToday.addAll(tomorrowFirst);
        }
        
        return upcomingToday;
    }
    
    /**
     * Get the currently running infusion, if any.
     */
    public Optional<SignageDisplayDTO> getCurrentInfusion() {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        
        List<InfusionSlot> todaySlots = slotRepository.findByScheduleDateAndNotCancelled(today);
        
        return todaySlots.stream()
                .filter(slot -> isCurrentlyRunning(slot, now))
                .findFirst()
                .map(slot -> toSignageDisplay(slot, now));
    }
    
    /**
     * Convert an InfusionSlot to a SignageDisplayDTO.
     */
    private SignageDisplayDTO toSignageDisplay(InfusionSlot slot, LocalTime now) {
        double avgIntensity = slot.getAverageHeatIntensity();
        
        // Get unique scent profiles from recipe steps
        List<String> scentProfiles = slot.getRecipe().getSteps().stream()
                .filter(step -> step.getIngredient() != null)
                .map(step -> step.getIngredient().getScentProfile())
                .distinct()
                .map(ScentProfile::name)
                .collect(Collectors.toList());
        
        return SignageDisplayDTO.builder()
                .roomName(slot.getRoom().getName())
                .recipeName(slot.getRecipe().getName())
                .startTime(slot.getStartTime().format(TIME_FORMATTER))
                .endTime(slot.getEndTime().format(TIME_FORMATTER))
                .duration(formatDuration(slot.getRecipe().calculateTotalDuration()))
                .intensity(mapIntensityLevel(avgIntensity))
                .intensityIcon(mapIntensityIcon(avgIntensity))
                .scentProfiles(scentProfiles)
                .theme(slot.getRecipe().getTheme())
                .isCurrentlyRunning(isCurrentlyRunning(slot, now))
                .build();
    }
    
    /**
     * Map average heat intensity to a guest-friendly intensity level.
     * 1-3: Mild
     * 4-6: Mittel
     * 7-10: Intensiv
     */
    private String mapIntensityLevel(double avgIntensity) {
        if (avgIntensity <= 3.0) {
            return "Mild";
        } else if (avgIntensity <= 6.0) {
            return "Mittel";
        } else {
            return "Intensiv";
        }
    }
    
    /**
     * Map average heat intensity to fire emoji icons.
     * 1-3: 🔥
     * 4-6: 🔥🔥
     * 7-10: 🔥🔥🔥
     */
    private String mapIntensityIcon(double avgIntensity) {
        if (avgIntensity <= 3.0) {
            return "🔥";
        } else if (avgIntensity <= 6.0) {
            return "🔥🔥";
        } else {
            return "🔥🔥🔥";
        }
    }
    
    /**
     * Format duration in seconds to a guest-friendly string like "20 min".
     */
    private String formatDuration(int durationSeconds) {
        int minutes = durationSeconds / 60;
        return minutes + " min";
    }
    
    /**
     * Check if a slot is currently running based on the current time.
     */
    private boolean isCurrentlyRunning(InfusionSlot slot, LocalTime now) {
        return !now.isBefore(slot.getStartTime()) && now.isBefore(slot.getEndTime());
    }
}
