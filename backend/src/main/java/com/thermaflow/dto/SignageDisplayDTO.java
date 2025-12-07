package com.thermaflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for displaying infusion information on digital signage screens.
 * Contains simplified, guest-friendly formatting optimized for display.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignageDisplayDTO {
    
    private String roomName;
    private String recipeName;
    private String startTime;        // Format: "14:00"
    private String endTime;          // Format: "14:20"
    private String duration;         // Format: "20 min"
    private String intensity;        // "Mild", "Mittel", "Intensiv"
    private String intensityIcon;    // "🔥", "🔥🔥", "🔥🔥🔥"
    private List<String> scentProfiles;  // e.g., ["CITRUS", "WOODY"]
    private String theme;            // Recipe theme for guest appeal
    private Boolean isCurrentlyRunning;
}
