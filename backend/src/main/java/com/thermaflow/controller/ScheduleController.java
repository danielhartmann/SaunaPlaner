package com.thermaflow.controller;

import com.thermaflow.dto.Conflict;
import com.thermaflow.dto.InfusionSlotDTO;
import com.thermaflow.dto.SlotMapper;
import com.thermaflow.model.DailySchedule;
import com.thermaflow.model.InfusionSlot;
import com.thermaflow.repository.*;
import com.thermaflow.service.InfusionSlotService;
import com.thermaflow.service.PdfExportService;
import com.thermaflow.service.ScheduleValidatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * REST controller for schedule management.
 */
@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ScheduleController {
    
    private final DailyScheduleRepository scheduleRepository;
    private final InfusionSlotRepository slotRepository;
    private final SaunaRoomRepository roomRepository;
    private final InfusionRecipeRepository recipeRepository;
    private final EmployeeRepository employeeRepository;
    private final InfusionSlotService slotService;
    private final ScheduleValidatorService validatorService;
    private final PdfExportService pdfExportService;
    private final SlotMapper slotMapper;
    
    @GetMapping("/{date}")
    public ResponseEntity<List<InfusionSlotDTO>> getScheduleByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        return scheduleRepository.findByDateWithSlots(date)
                .map(schedule -> schedule.getSlots().stream()
                        .map(slotMapper::toDTO)
                        .collect(Collectors.toList()))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.ok(List.of()));
    }
    
    @PostMapping("/{date}/slots")
    public ResponseEntity<?> createSlot(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestBody InfusionSlotDTO slotDTO) {
        
        // Get or create schedule
        DailySchedule schedule = scheduleRepository.findByDate(date)
                .orElseGet(() -> {
                    DailySchedule newSchedule = DailySchedule.builder()
                            .date(date)
                            .build();
                    return scheduleRepository.save(newSchedule);
                });
        
        // Build slot entity
        InfusionSlot slot = InfusionSlot.builder()
                .schedule(schedule)
                .room(roomRepository.findById(slotDTO.getRoomId())
                        .orElseThrow(() -> new IllegalArgumentException("Room not found")))
                .recipe(recipeRepository.findById(slotDTO.getRecipeId())
                        .orElseThrow(() -> new IllegalArgumentException("Recipe not found")))
                .employee(employeeRepository.findById(slotDTO.getEmployeeId())
                        .orElseThrow(() -> new IllegalArgumentException("Employee not found")))
                .startTime(slotDTO.getStartTime())
                .confirmed(false)
                .cancelled(false)
                .notes(slotDTO.getNotes())
                .build();
        
        try {
            InfusionSlot created = slotService.createAndConfirmSlot(slot);
            return ResponseEntity.ok(slotMapper.toDTO(created));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PostMapping("/slots/{slotId}/validate")
    public ResponseEntity<List<Conflict>> validateSlot(@PathVariable Long slotId) {
        InfusionSlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new IllegalArgumentException("Slot not found"));
        
        List<Conflict> conflicts = validatorService.validate(slot);
        return ResponseEntity.ok(conflicts);
    }
    
    @PostMapping("/slots/{slotId}/confirm")
    public ResponseEntity<InfusionSlotDTO> confirmSlot(@PathVariable Long slotId) {
        try {
            InfusionSlot confirmed = slotService.confirmSlot(slotId);
            return ResponseEntity.ok(slotMapper.toDTO(confirmed));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/slots/{slotId}")
    public ResponseEntity<Void> cancelSlot(
            @PathVariable Long slotId,
            @RequestParam(defaultValue = "true") boolean restoreInventory) {
        
        try {
            slotService.cancelSlot(slotId, restoreInventory);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/{date}/pdf")
    public CompletableFuture<ResponseEntity<byte[]>> downloadSchedulePdf(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        DailySchedule schedule = scheduleRepository.findByDateWithSlots(date)
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found for date: " + date));
        
        return pdfExportService.generateDailySchedulePdf(schedule)
                .thenApply(pdfBytes -> ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, 
                                "attachment; filename=schedule-" + date + ".pdf")
                        .contentType(MediaType.APPLICATION_PDF)
                        .body(pdfBytes))
                .exceptionally(ex -> ResponseEntity.internalServerError().build());
    }
}
