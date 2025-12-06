# ThermaFlow - Architecture Documentation

## Overview

ThermaFlow is a comprehensive, enterprise-grade SaaS platform for thermal bath sauna infusion management. The system is designed with a modular architecture that separates concerns into distinct, well-defined modules.

## System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                      FRONTEND LAYER                          │
│                    Angular 19 (Port 4200)                    │
├─────────────────────────────────────────────────────────────┤
│  ┌─────────────────┐  ┌─────────────────┐  ┌──────────────┐│
│  │ Recipe Builder  │  │ Schedule Planner│  │ PDF Viewer   ││
│  │   Component     │  │   Component     │  │  Component   ││
│  │   (Signals)     │  │   (Signals)     │  │              ││
│  └─────────────────┘  └─────────────────┘  └──────────────┘│
└─────────────────────────────────────────────────────────────┘
                              ↕
                        REST API (HTTP)
                              ↕
┌─────────────────────────────────────────────────────────────┐
│                      BACKEND LAYER                           │
│              Spring Boot 3.4 + Java 17 (Port 8080)          │
├─────────────────────────────────────────────────────────────┤
│  ┌────────────────────────────────────────────────────────┐ │
│  │              REST Controllers Layer                     │ │
│  │  RecipeController | ScheduleController | PDF Download  │ │
│  └────────────────────────────────────────────────────────┘ │
│                              ↕                               │
│  ┌────────────────────────────────────────────────────────┐ │
│  │              Service Layer (Business Logic)            │ │
│  │  ┌──────────────┐  ┌────────────────┐  ┌────────────┐ │ │
│  │  │ Recipe Svc   │  │ Validator Svc  │  │  PDF Svc   │ │ │
│  │  │              │  │  (Conflict Det)│  │ (Async VT) │ │ │
│  │  └──────────────┘  └────────────────┘  └────────────┘ │ │
│  │  ┌──────────────────────────────────────────────────┐  │ │
│  │  │        InfusionSlotService                       │  │ │
│  │  │   (Transactional Inventory Management)          │  │ │
│  │  └──────────────────────────────────────────────────┘  │ │
│  └────────────────────────────────────────────────────────┘ │
│                              ↕                               │
│  ┌────────────────────────────────────────────────────────┐ │
│  │         Repository Layer (Spring Data JPA)             │ │
│  │   6 Repositories with Custom Queries                   │ │
│  └────────────────────────────────────────────────────────┘ │
│                              ↕                               │
│  ┌────────────────────────────────────────────────────────┐ │
│  │              Domain Model (JPA Entities)               │ │
│  │  ┌──────┐  ┌──────────┐  ┌──────────┐  ┌────────────┐│ │
│  │  │Recipe│  │Ingredient│  │SaunaRoom │  │  Employee  ││ │
│  │  └──────┘  └──────────┘  └──────────┘  └────────────┘│ │
│  │  ┌──────────────┐  ┌──────────────┐                   │ │
│  │  │InfusionSlot  │  │DailySchedule │                   │ │
│  │  └──────────────┘  └──────────────┘                   │ │
│  └────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
                              ↕
┌─────────────────────────────────────────────────────────────┐
│                    DATABASE LAYER                            │
│                  PostgreSQL / H2 (In-Memory)                 │
│                    Managed by Flyway                         │
└─────────────────────────────────────────────────────────────┘
```

## Module Breakdown

### Module A: Infusion Designer (Recipe Management)

**Purpose**: Manage complex, multi-step sauna infusion recipes with ingredients.

**Key Components**:
- `InfusionRecipe` - Container for recipe metadata and steps
- `InfusionStep` - Individual round with duration, heat, scent, music, lighting
- `Ingredient` - Scent ingredients with stock tracking and cost

**Key Features**:
- Calculated fields using domain logic (totalDuration, totalCost)
- @OneToMany relationships with proper cascade and ordering
- Support for complex recipes with multiple rounds
- Ingredient categorization by scent profile (CITRUS, WOODY, FLORAL, HERBAL)

**Code Example**:
```java
public Integer calculateTotalDuration() {
    return steps.stream()
            .mapToInt(InfusionStep::getDurationSeconds)
            .sum();
}

public BigDecimal calculateTotalCost() {
    return steps.stream()
            .filter(step -> step.getIngredient() != null)
            .map(step -> step.getIngredient().getCostPerMl()
                    .multiply(BigDecimal.valueOf(step.getScentDosageMl())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
}
```

### Module B: Resource & Staff Logistics

**Purpose**: Manage sauna rooms and staff with skills and constraints.

**Key Components**:
- `SaunaRoom` - Physical rooms with capacity, type, cool-down requirements
- `Employee` - Staff with certification, skills, and health limits
- `ShiftPlan` - Employee shift scheduling

**Key Features**:
- Skill-based assignment (SINGING_BOWL, WENIK, HIGH_HEAT)
- Cool-down period management (ventilation between sessions)
- Daily max infusions for employee health and safety
- Room capabilities (sound system, capacity, type)

**Enum Types**:
```java
public enum SaunaType {
    KELO, FINNISH, BIO, STEAM, INFRARED
}

public enum EmployeeSkill {
    SINGING_BOWL, WENIK, HIGH_HEAT, AROMATHERAPY, MEDITATION
}
```

### Module C: The Planner (Scheduling Core)

**Purpose**: Schedule infusion sessions with comprehensive conflict detection.

**Key Components**:
- `DailySchedule` - Container for all slots on a date
- `InfusionSlot` - Scheduled session linking room, recipe, employee, time
- `ScheduleValidatorService` - Conflict detection engine

**Conflict Detection**:
The validator uses Java Streams to efficiently check for:
1. **Employee Conflicts** - Staff already assigned at overlapping time
2. **Room Conflicts** - Room occupied during requested slot
3. **Cool-down Violations** - Insufficient ventilation time between sessions
4. **Inventory Shortage** - Insufficient ingredients for recipe
5. **Health Constraints** - Employee exceeding daily max infusions

**Code Example**:
```java
public List<Conflict> validate(InfusionSlot newSlot) {
    List<Conflict> conflicts = new ArrayList<>();
    
    // Get existing slots for the same date
    List<InfusionSlot> existingSlots = slotRepository
            .findByScheduleDateAndNotCancelled(newSlot.getSchedule().getDate());
    
    // Calculate new slot time boundaries
    LocalTime newSlotStart = newSlot.getStartTime();
    LocalTime newSlotEnd = newSlot.getEndTime();
    
    // Check employee availability
    conflicts.addAll(validateEmployeeAvailability(...));
    
    // Check room availability with cool-down
    conflicts.addAll(validateRoomAvailability(...));
    
    // Check inventory
    conflicts.addAll(validateInventory(newSlot));
    
    return conflicts;
}
```

**Cool-down Logic**:
```java
public LocalTime getEndTimeWithCoolDown() {
    return getEndTime().plusMinutes(room.getRequiredCoolDownMin());
}

// Validate that new slot doesn't start before cool-down period ends
if (timesOverlap(existingStart, existingEndWithCoolDown, 
                 newSlotStart, newSlotEndWithCoolDown)) {
    // Conflict detected
}
```

### Module D: PDF Generation

**Purpose**: Generate professional daily schedule PDFs for guest display.

**Key Components**:
- `PdfExportService` - Async PDF generation service
- OpenPDF library for document creation

**Key Features**:
- **Async Execution** - Runs on dedicated executor (Virtual Threads on Java 21+)
- **Formatted Tables** - Professional layout with headers and data rows
- **Visual Indicators** - Intensity shown with flame symbols (🔥, 🔥🔥, 🔥🔥🔥)
- **QR Code Placeholders** - For guest rating system integration

**Code Example**:
```java
@Async("virtualThreadExecutor")
public CompletableFuture<byte[]> generateDailySchedulePdf(DailySchedule schedule) {
    try {
        byte[] pdfBytes = createPdf(schedule);
        return CompletableFuture.completedFuture(pdfBytes);
    } catch (Exception e) {
        return CompletableFuture.failedFuture(e);
    }
}

private String getIntensityIndicator(double averageIntensity) {
    if (averageIntensity <= 3.0) return "🔥";
    else if (averageIntensity <= 6.0) return "🔥🔥";
    else return "🔥🔥🔥";
}
```

## Transactional Inventory Management

One of the most critical features is the transactional inventory deduction system.

### Problem Statement
When an infusion slot is confirmed, ingredients must be deducted from stock. This operation must be:
- **Atomic** - Either all deductions succeed or none
- **Consistent** - Inventory levels remain accurate
- **Isolated** - Concurrent operations don't interfere
- **Durable** - Changes are permanent once committed

### Solution Architecture

```java
@Transactional
public InfusionSlot confirmSlot(Long slotId) {
    InfusionSlot slot = slotRepository.findById(slotId)
            .orElseThrow(() -> new IllegalArgumentException("Slot not found"));
    
    // Validate inventory before confirming
    List<Conflict> inventoryConflicts = validatorService.validate(slot).stream()
            .filter(c -> c.getType() == Conflict.ConflictType.INSUFFICIENT_INVENTORY)
            .toList();
    
    if (!inventoryConflicts.isEmpty()) {
        throw new IllegalStateException("Cannot confirm slot: insufficient inventory");
    }
    
    // Deduct inventory atomically
    deductInventory(slot);
    
    // Mark as confirmed
    slot.setConfirmed(true);
    return slotRepository.save(slot);
}
```

### Transaction Flow
1. **Validation Phase** - Check inventory availability
2. **Deduction Phase** - Deduct all required ingredients
3. **Commit Phase** - Mark slot as confirmed
4. **Rollback** - If any step fails, entire transaction rolls back

### Restoration Support
Cancelled slots can optionally restore inventory:
```java
@Transactional
public InfusionSlot cancelSlot(Long slotId, boolean restoreInventory) {
    InfusionSlot slot = slotRepository.findById(slotId)
            .orElseThrow();
    
    if (restoreInventory && slot.getConfirmed()) {
        restoreInventory(slot);
    }
    
    slot.setCancelled(true);
    return slotRepository.save(slot);
}
```

## Frontend Architecture (Angular 19)

### RecipeBuilderComponent

**Technology Highlights**:
- **Standalone Component** - No NgModules required
- **Signals** - Reactive state management without RxJS Subjects
- **Computed Signals** - Automatic recalculation of totals
- **New Control Flow** - @if and @for syntax

**State Management with Signals**:
```typescript
// Signal for steps array
steps = signal<InfusionStep[]>([]);

// Computed signal - automatically recalculates when steps change
totalDuration = computed(() => {
    return this.steps().reduce((sum, step) => 
        sum + (step.durationSeconds || 0), 0
    );
});

totalCost = computed(() => {
    return this.steps().reduce((sum, step) => {
        if (step.ingredientId && step.scentDosageMl) {
            const ingredient = this.availableIngredients()
                .find(i => i.id === step.ingredientId);
            if (ingredient) {
                return sum + (ingredient.costPerMl * step.scentDosageMl);
            }
        }
        return sum;
    }, 0);
});
```

**Dynamic Form Management**:
```typescript
// Add step - creates new array reference to trigger signal update
addStep(): void {
    const newStep: InfusionStep = {
        name: `Round ${this.steps().length + 1}`,
        durationSeconds: 300,
        heatIntensity: 5,
        scentDosageMl: 50,
        stepOrder: this.steps().length
    };
    
    this.steps.update(steps => [...steps, newStep]);
}

// Remove step - filter and reorder
removeStep(stepOrder: number): void {
    this.steps.update(steps => {
        const filtered = steps.filter(s => s.stepOrder !== stepOrder);
        return filtered.map((step, index) => ({ ...step, stepOrder: index }));
    });
}
```

**Template with New Control Flow**:
```html
<!-- @for directive -->
@for (step of steps(); track step.stepOrder) {
    <mat-card class="step-card">
        <mat-card-header>
            <mat-card-title>{{ step.name }}</mat-card-title>
        </mat-card-header>
        <mat-card-content>
            <!-- Form fields -->
        </mat-card-content>
    </mat-card>
}

<!-- @if directive -->
@if (steps().length === 0) {
    <div class="empty-state">
        <p>No steps added yet.</p>
    </div>
}
```

## Database Schema

### Key Tables
- `ingredients` - Scent ingredients with stock tracking
- `infusion_recipes` - Recipe metadata
- `infusion_steps` - Recipe steps (FK to recipes and ingredients)
- `sauna_rooms` - Physical room information
- `employees` - Staff information
- `employee_skills` - Many-to-many skill mapping
- `shift_plans` - Employee shift schedules
- `daily_schedules` - Daily schedule container
- `infusion_slots` - Scheduled sessions

### Migration Strategy
Flyway manages database versioning:
- `V1__initial_schema.sql` - Create all tables and indexes
- `V2__sample_data.sql` - Insert test data

### Performance Optimizations
```sql
-- Indexes for common queries
CREATE INDEX idx_infusion_steps_recipe ON infusion_steps(recipe_id);
CREATE INDEX idx_shift_plans_employee_date ON shift_plans(employee_id, date);
CREATE INDEX idx_infusion_slots_schedule ON infusion_slots(schedule_id);
CREATE INDEX idx_infusion_slots_room_schedule ON infusion_slots(room_id, schedule_id);
CREATE INDEX idx_infusion_slots_employee_schedule ON infusion_slots(employee_id, schedule_id);
```

## API Endpoints

### Recipe Management
- `GET /api/recipes` - List all recipes with calculated totals
- `GET /api/recipes/{id}` - Get single recipe
- `POST /api/recipes` - Create new recipe
- `DELETE /api/recipes/{id}` - Delete recipe

### Ingredient Management
- `GET /api/ingredients` - List all ingredients
- `POST /api/ingredients` - Create ingredient
- `PUT /api/ingredients/{id}` - Update ingredient (including stock)

### Schedule Management
- `GET /api/schedules/{date}` - Get all slots for date
- `POST /api/schedules/{date}/slots` - Create and validate new slot
- `POST /api/schedules/slots/{slotId}/validate` - Validate existing slot
- `POST /api/schedules/slots/{slotId}/confirm` - Confirm slot (deduct inventory)
- `DELETE /api/schedules/slots/{slotId}?restoreInventory=true` - Cancel slot

### PDF Export
- `GET /api/schedules/{date}/pdf` - Download daily schedule as PDF (async)

## Technology Decisions

### Why Java 17 (vs Java 21)?
- Java 17 is LTS with widespread enterprise support
- Virtual Threads in Java 21 are optional enhancement
- Async executor works efficiently on Java 17
- Easy upgrade path to Java 21 when ready

### Why Signals (vs RxJS)?
- Simpler mental model
- Better performance (no subscription overhead)
- Native Angular 19 feature
- Automatic dependency tracking

### Why MapStruct (vs Manual Mapping)?
- Compile-time type safety
- Zero runtime overhead
- Maintainable generated code
- Integrates with Lombok

### Why OpenPDF (vs iText)?
- Open source (LGPL/MPL)
- No licensing concerns
- Mature and stable
- Good documentation

### Why Flyway (vs Liquibase)?
- Simpler SQL-based migrations
- Better performance
- Easier to review and debug
- Industry standard

## Security Considerations

### Current Implementation
- CORS enabled for development (`@CrossOrigin(origins = "*")`)
- H2 console enabled for development only
- Transaction isolation for inventory operations

### Production Recommendations
1. **Authentication/Authorization** - Add Spring Security with JWT
2. **CORS** - Restrict to production domain
3. **Rate Limiting** - Add request throttling
4. **Input Validation** - Enhance with @Valid and custom validators
5. **Audit Logging** - Track all inventory changes
6. **SSL/TLS** - Enforce HTTPS in production
7. **Database** - Use PostgreSQL with connection pooling
8. **Secrets Management** - Use environment variables or vault

## Performance Optimizations

### Current
- Batch loading with `@Query` and `JOIN FETCH`
- Indexed queries for common operations
- Lazy loading for large collections
- Connection pooling (HikariCP)

### Future Enhancements
- Redis caching for recipes and schedules
- Database query result caching
- Pagination for large result sets
- WebSocket for real-time updates
- CDN for PDF delivery

## Deployment

### Docker Deployment
```dockerfile
FROM eclipse-temurin:17-jre
COPY target/thermaflow-backend-1.0.0-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Environment Variables
```bash
DATABASE_URL=jdbc:postgresql://localhost:5432/thermaflow
DATABASE_USERNAME=thermaflow_user
DATABASE_PASSWORD=secure_password
DATABASE_DRIVER=org.postgresql.Driver
HIBERNATE_DIALECT=org.hibernate.dialect.PostgreSQLDialect
```

### Running Locally
```bash
# Backend
cd backend
mvn spring-boot:run

# Frontend
cd frontend
npm install
npm start
```

## Testing Strategy

### Unit Tests
- Service layer logic (ScheduleValidatorService)
- Domain model calculations (InfusionRecipe)
- MapStruct mappers

### Integration Tests
- Repository queries
- Controller endpoints
- Transaction boundaries

### Current Coverage
- `ScheduleValidatorServiceTest` - Conflict detection
- `ThermaFlowApplicationTests` - Application context loading

## Future Roadmap

1. **Phase 1** - Core Features (Complete ✓)
   - Recipe management
   - Schedule planning
   - Conflict detection
   - PDF generation

2. **Phase 2** - Enhanced Features
   - Real-time schedule updates (WebSocket)
   - Mobile app for employees
   - Guest rating system
   - Analytics dashboard

3. **Phase 3** - Integrations
   - DMX lighting system control
   - Music streaming integration
   - Automated inventory ordering
   - Multi-language support

4. **Phase 4** - Enterprise
   - Multi-tenant support
   - Advanced analytics
   - Machine learning for optimal scheduling
   - Revenue management system
