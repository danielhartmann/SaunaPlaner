# ThermaFlow - High-End Sauna Infusion Management SaaS Platform

## Overview

ThermaFlow is a comprehensive SaaS platform designed for thermal baths to plan, manage, and display sauna infusions (Aufgüsse). The platform handles logistics (staff/rooms), creativity (recipes), and guest communication (PDF/Displays) in a modular, highly customizable architecture.

## Technology Stack

### Backend
- **Java 17+** (or Java 21 LTS for full Virtual Threads support) with Spring Boot 3.4+
- **Virtual Threads** enabled for heavy I/O operations (PDF generation, database reporting)
- **PostgreSQL** (Production) / **H2** (Development)
- **Spring Data JPA** with Flyway migrations
- **Lombok** for boilerplate reduction
- **MapStruct** for DTO mapping
- **OpenPDF** for PDF generation

### Frontend
- **Angular 19**
- **Standalone Components** architecture
- **Signals** for state management (no RxJS Subjects)
- **New Control Flow** syntax (@if, @for)
- **Angular Material** for UI components

## Architecture

### Module A: Infusion Designer (Recipe Management)

**Entities:**
- `InfusionRecipe`: Main recipe entity with calculated fields
- `Ingredient`: Scent ingredients with viscosity, scent profile, stock levels, and cost
- `InfusionStep`: Individual recipe steps with duration, heat intensity, scent dosage, music, and lighting

**Key Features:**
- Complex multi-step recipes with ordering
- Automatic calculation of total duration and cost
- One-to-many relationships efficiently modeled with JPA
- Ingredient tracking with scent profiles (CITRUS, WOODY, FLORAL, HERBAL)

### Module B: Resource & Staff Logistics

**Entities:**
- `SaunaRoom`: Room properties including capacity, type (KELO, FINNISH, BIO), sound system, and cool-down requirements
- `Employee`: Staff with certification levels, daily max infusions, and skills
- `ShiftPlan`: Employee shift scheduling

**Key Features:**
- Skill-based assignment (SINGING_BOWL, WENIK, HIGH_HEAT)
- Health safety constraints (daily max infusions)
- Room capabilities and requirements

### Module C: The Planner (Scheduling Core)

**Entities:**
- `DailySchedule`: Container for all slots on a specific date
- `InfusionSlot`: Scheduled sessions connecting room, recipe, employee, and time

**Key Features:**
- **ScheduleValidatorService**: Comprehensive conflict detection using Java Streams
  - Employee availability checks
  - Room availability checks
  - Cool-down period enforcement
  - Inventory validation
- **Transactional Inventory Deduction**: Atomic stock management
- Conflict types: EMPLOYEE_UNAVAILABLE, EMPLOYEE_MAX_INFUSIONS_EXCEEDED, EMPLOYEE_SKILL_MISSING, ROOM_OCCUPIED, ROOM_COOLDOWN_VIOLATION, INSUFFICIENT_INVENTORY

### Module D: PDF Generation

**Key Features:**
- **PdfExportService** running on Virtual Threads (@Async with virtualThreadExecutor)
- Daily schedule PDF with formatted tables
- Visual intensity indicators (🔥 symbols based on heat level)
- QR code placeholders for guest rating system
- Professional layout with headers, formatted columns, and guest information

## Inventory Deduction Architecture

**Transactional Safety:**
The inventory deduction is handled transactionally in `InfusionSlotService`:

1. **Validation First**: Check inventory availability before confirming
2. **Atomic Deduction**: All ingredient deductions happen within a single transaction
3. **Rollback on Error**: If any deduction fails, the entire transaction rolls back
4. **Pessimistic Locking**: Prevents concurrent inventory deductions (via JPA)
5. **Restoration**: Cancelled slots can optionally restore inventory

```java
@Transactional
public InfusionSlot confirmSlot(Long slotId) {
    // Validate, then deduct inventory atomically
    // If any step fails, entire transaction rolls back
}
```

## Virtual Threads Configuration

Virtual Threads are enabled for I/O-heavy operations:

```yaml
spring:
  threads:
    virtual:
      enabled: true
```

Custom executor configured for async operations:
```java
@Bean(name = "virtualThreadExecutor")
public Executor virtualThreadExecutor() {
    executor.setVirtualThreads(true);
    // Used for PDF generation and heavy I/O
}
```

## Frontend Architecture

### RecipeBuilderComponent

**Key Features:**
- **Signals for State Management**: Reactive state without RxJS Subjects
- **Computed Signals**: Automatic calculation of totals
  ```typescript
  totalDuration = computed(() => 
    this.steps().reduce((sum, step) => sum + step.durationSeconds, 0)
  );
  
  totalCost = computed(() => 
    this.steps().reduce((sum, step) => {
      const ingredient = this.availableIngredients().find(i => i.id === step.ingredientId);
      return sum + (ingredient ? ingredient.costPerMl * step.scentDosageMl : 0);
    }, 0)
  );
  ```
- **Dynamic Form**: Signal-based step management
- **New Control Flow**: @if and @for syntax
- **Material UI**: Professional, accessible interface

## API Endpoints

### Recipe Management
- `GET /api/recipes` - List all recipes
- `GET /api/recipes/{id}` - Get recipe by ID
- `POST /api/recipes` - Create recipe
- `DELETE /api/recipes/{id}` - Delete recipe

### Schedule Management
- `GET /api/schedules/{date}` - Get schedule for date
- `POST /api/schedules/{date}/slots` - Create new slot
- `POST /api/schedules/slots/{slotId}/validate` - Validate slot
- `POST /api/schedules/slots/{slotId}/confirm` - Confirm slot (deduct inventory)
- `DELETE /api/schedules/slots/{slotId}` - Cancel slot
- `GET /api/schedules/{date}/pdf` - Download daily schedule PDF

### Digital Signage (Guest-Facing Displays)
- `GET /api/signage/today` - Today's schedule formatted for displays
- `GET /api/signage/{date}` - Schedule for specific date (format: yyyy-MM-dd)
- `GET /api/signage/next` - Next 3-5 upcoming infusions
- `GET /api/signage/current` - Currently running infusion (404 if none)

**Display Format Features:**
- Guest-friendly time formatting (HH:mm)
- Duration in minutes (e.g., "20 min")
- Intensity levels: Mild 🔥, Mittel 🔥🔥, Intensiv 🔥🔥🔥
- Scent profiles and themes
- Currently running status indicator

## Database Schema

Flyway migrations manage the schema:
- **V1__initial_schema.sql**: Core tables and indexes
- **V2__sample_data.sql**: Sample data for testing

## Building and Running

### Backend
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

Backend runs on http://localhost:8080

### Frontend
```bash
cd frontend
npm install
npm start
```

Frontend runs on http://localhost:4200

### H2 Console (Development)
Access at http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:thermaflow`
- Username: `sa`
- Password: (empty)

## Testing

```bash
cd backend
mvn test
```

Tests include:
- ScheduleValidatorService tests for conflict detection
- Validation of employee conflicts
- Room cooldown violation checks

## Key Design Decisions

1. **Virtual Threads**: Chosen for PDF generation as it's I/O-heavy and benefits from lightweight concurrency
2. **Signals over RxJS**: Modern Angular 19 approach for simpler, more performant state management
3. **MapStruct**: Compile-time DTO mapping for type safety and performance
4. **Transactional Inventory**: Ensures data consistency and prevents double-deduction
5. **Computed Fields**: Recipe totals calculated dynamically in both backend (JPA) and frontend (Signals)
6. **Standalone Components**: Modern Angular architecture without NgModules

## Future Enhancements

- WebSocket for real-time schedule updates
- QR code generation and rating system integration
- Mobile app for employees
- Advanced analytics dashboard
- Multi-language support
- Integration with DMX lighting systems
- Music streaming integration