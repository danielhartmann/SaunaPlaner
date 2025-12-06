package com.thermaflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * ThermaFlow - High-end SaaS platform for thermal baths sauna infusion management.
 * 
 * Key Features:
 * - Virtual Threads enabled for heavy I/O operations (PDF generation, DB reporting)
 * - Modular architecture for logistics, creativity, and guest communication
 * - PostgreSQL in production, H2 for development
 */
@SpringBootApplication
@EnableAsync
public class ThermaFlowApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(ThermaFlowApplication.class, args);
    }
}
