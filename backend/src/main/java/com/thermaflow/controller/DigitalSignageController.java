package com.thermaflow.controller;

import com.thermaflow.dto.SignageDisplayDTO;
import com.thermaflow.service.DigitalSignageService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for digital signage displays.
 * Provides guest-friendly, formatted infusion schedule data optimized for screens.
 */
@RestController
@RequestMapping("/api/signage")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DigitalSignageController {
    
    private final DigitalSignageService signageService;
    
    /**
     * Get today's complete schedule formatted for signage display.
     * 
     * @return List of all infusions scheduled for today
     */
    @GetMapping("/today")
    public ResponseEntity<List<SignageDisplayDTO>> getTodaySchedule() {
        List<SignageDisplayDTO> schedule = signageService.getTodaySchedule();
        return ResponseEntity.ok(schedule);
    }
    
    /**
     * Get schedule for a specific date formatted for signage display.
     * 
     * @param date The date to retrieve the schedule for (format: yyyy-MM-dd)
     * @return List of all infusions scheduled for the specified date
     */
    @GetMapping("/{date}")
    public ResponseEntity<List<SignageDisplayDTO>> getScheduleByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<SignageDisplayDTO> schedule = signageService.getScheduleForDate(date);
        return ResponseEntity.ok(schedule);
    }
    
    /**
     * Get the next 3-5 upcoming infusions.
     * Useful for "Coming Up Next" displays in the lobby.
     * 
     * @return List of the next 3-5 upcoming infusions
     */
    @GetMapping("/next")
    public ResponseEntity<List<SignageDisplayDTO>> getNextInfusions() {
        List<SignageDisplayDTO> nextInfusions = signageService.getNextInfusions();
        return ResponseEntity.ok(nextInfusions);
    }
    
    /**
     * Get the currently running infusion, if any.
     * Useful for "Now Playing" displays.
     * 
     * @return Currently running infusion, or 404 if none is running
     */
    @GetMapping("/current")
    public ResponseEntity<SignageDisplayDTO> getCurrentInfusion() {
        return signageService.getCurrentInfusion()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
