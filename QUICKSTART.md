# ThermaFlow - Quick Start Guide

## Prerequisites

- **Java 17+** (Java 21 recommended for Virtual Threads support)
- **Maven 3.6+**
- **Node.js 18+** and npm (for frontend)
- **Git**

## Backend Setup (5 minutes)

### 1. Clone and Navigate
```bash
git clone https://github.com/danielhartmann/SaunaPlaner.git
cd SaunaPlaner/backend
```

### 2. Build the Application
```bash
mvn clean install
```

### 3. Run the Application
```bash
mvn spring-boot:run
```

The backend will start on **http://localhost:8080**

### 4. Verify Backend is Running
```bash
curl http://localhost:8080/api/recipes
```

You should see a JSON response with sample recipes.

### 5. Access H2 Console (Development Database)
Open your browser and navigate to:
- URL: **http://localhost:8080/h2-console**
- JDBC URL: `jdbc:h2:mem:thermaflow`
- Username: `sa`
- Password: (leave empty)

## Frontend Setup (5 minutes)

### 1. Navigate to Frontend
```bash
cd ../frontend
```

### 2. Install Dependencies
```bash
npm install
```

### 3. Start Development Server
```bash
npm start
```

The frontend will start on **http://localhost:4200**

### 4. Open in Browser
Navigate to **http://localhost:4200** to see the Recipe Builder interface.

## Quick Tour of Key Features

### 1. Recipe Builder (Frontend)

**Try creating a recipe:**
1. Open http://localhost:4200
2. Fill in Recipe Name: "My First Ritual"
3. Fill in Theme: "Test Experience"
4. Click "Add Step" to create rounds
5. Fill in step details:
   - Duration: 300 seconds
   - Heat Intensity: 5 (1-10 scale)
   - Scent Dosage: 50 ml
   - Select an ingredient
6. Notice the **automatic calculation** of Total Duration and Total Cost at the bottom
7. Click "Save Recipe"

**Key Features to Notice:**
- ✅ Total Duration updates automatically as you change step durations
- ✅ Total Cost recalculates when you change dosages or ingredients
- ✅ You can add multiple steps (rounds)
- ✅ Form validation prevents invalid data

### 2. View Recipes (Backend API)

```bash
# Get all recipes
curl http://localhost:8080/api/recipes

# Get specific recipe
curl http://localhost:8080/api/recipes/1
```

### 3. View Sample Data

The application comes with sample data including:
- **5 Ingredients**: Eucalyptus, Lavender, Pine, Citrus, Sandalwood
- **3 Sauna Rooms**: Aurora Finnish, Kelo Wood, Bio Sauna
- **3 Employees**: Anna, Max, Sophie (with different skills)
- **3 Sample Recipes**: Nordic Aurora, Forest Awakening, Citrus Energy

### 4. Test Schedule Validation

Create a daily schedule and validate conflicts:

```bash
# Create a schedule slot (note: update IDs based on your data)
curl -X POST http://localhost:8080/api/schedules/2025-12-15/slots \
  -H "Content-Type: application/json" \
  -d '{
    "roomId": 1,
    "recipeId": 1,
    "employeeId": 1,
    "startTime": "10:00:00",
    "notes": "Morning session"
  }'
```

**The validator checks:**
- ✅ Employee availability (no double-booking)
- ✅ Room availability (no conflicts)
- ✅ Cool-down period (ventilation time between sessions)
- ✅ Ingredient inventory (sufficient stock)

### 5. Generate PDF Schedule

```bash
# First create a schedule with slots, then:
curl http://localhost:8080/api/schedules/2025-12-15/pdf \
  --output daily-schedule.pdf
```

Open `daily-schedule.pdf` to see the professionally formatted daily plan with:
- Time slots
- Room locations
- Recipe themes
- Intensity indicators (🔥 symbols)
- QR code placeholders

## Key API Endpoints

### Recipes
- `GET /api/recipes` - List all recipes
- `POST /api/recipes` - Create recipe
- `GET /api/recipes/{id}` - Get recipe details
- `DELETE /api/recipes/{id}` - Delete recipe

### Ingredients
- `GET /api/ingredients` - List all ingredients
- `POST /api/ingredients` - Create ingredient
- `PUT /api/ingredients/{id}` - Update ingredient stock

### Schedules
- `GET /api/schedules/{date}` - Get schedule for date (format: YYYY-MM-DD)
- `POST /api/schedules/{date}/slots` - Create slot
- `POST /api/schedules/slots/{slotId}/validate` - Validate slot
- `POST /api/schedules/slots/{slotId}/confirm` - Confirm (deducts inventory)
- `DELETE /api/schedules/slots/{slotId}` - Cancel slot
- `GET /api/schedules/{date}/pdf` - Download PDF

## Understanding the Conflict Detection

Try creating overlapping slots to see conflict detection in action:

```bash
# Create first slot at 10:00
curl -X POST http://localhost:8080/api/schedules/2025-12-15/slots \
  -H "Content-Type: application/json" \
  -d '{
    "roomId": 1,
    "recipeId": 1,
    "employeeId": 1,
    "startTime": "10:00:00"
  }'

# Try creating overlapping slot at 10:03 (will fail with conflict)
curl -X POST http://localhost:8080/api/schedules/2025-12-15/slots \
  -H "Content-Type: application/json" \
  -d '{
    "roomId": 1,
    "recipeId": 1,
    "employeeId": 1,
    "startTime": "10:03:00"
  }'
```

The second request will return a conflict error because:
- Recipe 1 (Nordic Aurora) takes about 720 seconds (12 minutes)
- Room 1 has a 15-minute cool-down requirement
- Total occupied time: 12 + 15 = 27 minutes (until 10:27)
- New slot at 10:03 overlaps with this period

## Understanding Inventory Management

```bash
# Check current ingredient stock
curl http://localhost:8080/api/ingredients

# Confirm a slot (deducts inventory)
curl -X POST http://localhost:8080/api/schedules/slots/1/confirm

# Check ingredient stock again - it should be reduced
curl http://localhost:8080/api/ingredients

# Cancel with inventory restoration
curl -X DELETE "http://localhost:8080/api/schedules/slots/1?restoreInventory=true"

# Check stock again - it should be restored
curl http://localhost:8080/api/ingredients
```

## Running Tests

### Backend Tests
```bash
cd backend
mvn test
```

Tests include:
- ✅ Application context loading
- ✅ Schedule validator service (conflict detection)
- ✅ Employee availability checks
- ✅ Room cool-down validation

## Production Configuration

For production deployment, update `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://your-db-host:5432/thermaflow_prod
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    driver-class-name: org.postgresql.Driver
  
  jpa:
    hibernate:
      ddl-auto: validate  # Never use 'create' or 'update' in production
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
  
  h2:
    console:
      enabled: false  # Disable H2 console in production
```

### Environment Variables
Set these in your production environment:
```bash
export DATABASE_URL=jdbc:postgresql://your-db:5432/thermaflow_prod
export DATABASE_USERNAME=thermaflow_user
export DATABASE_PASSWORD=secure_password
```

## Troubleshooting

### Backend won't start
- **Check Java version**: Run `java -version` (need Java 17+)
- **Port 8080 in use**: Either stop the other app or change port in `application.yml`
- **Database connection**: H2 should work out of box for development

### Frontend won't start
- **Check Node.js**: Run `node -v` (need Node 18+)
- **Dependencies not installed**: Run `npm install` again
- **Port 4200 in use**: Stop other Angular apps or change port: `ng serve --port 4201`

### Can't see recipes
- **Backend not running**: Make sure backend is running on port 8080
- **CORS issues**: Backend has CORS enabled for development
- **Check browser console**: Open DevTools (F12) to see any errors

### Tests failing
- **Build first**: Run `mvn clean install` before testing
- **Java version**: Some tests may require Java 17+

## Next Steps

1. **Explore the Code**
   - Check out `ARCHITECTURE.md` for detailed technical documentation
   - Review the domain models in `backend/src/main/java/com/thermaflow/model/`
   - Examine the validators in `backend/src/main/java/com/thermaflow/service/`

2. **Customize**
   - Add your own ingredients
   - Create custom recipes
   - Configure sauna rooms for your facility
   - Add employees with their skills

3. **Extend**
   - Add authentication (Spring Security)
   - Integrate with real DMX lighting systems
   - Add music playlist integration
   - Build mobile app for staff

4. **Deploy**
   - Package as Docker container
   - Deploy to cloud (AWS, Azure, GCP)
   - Set up PostgreSQL database
   - Configure CI/CD pipeline

## Getting Help

- **Documentation**: See `README.md` and `ARCHITECTURE.md`
- **Issues**: Create an issue on GitHub
- **Code**: All code is well-commented with JavaDoc and inline comments

## Key Concepts to Understand

1. **Signals in Angular**: Reactive state without RxJS complexity
2. **Computed Values**: Automatic recalculation (totalDuration, totalCost)
3. **Transactional Inventory**: ACID compliance for stock management
4. **Conflict Detection**: Multi-faceted validation using Java Streams
5. **Cool-down Periods**: Essential for room ventilation and safety
6. **Virtual Threads**: Optional Java 21 feature for better async performance

Enjoy building with ThermaFlow! 🔥
