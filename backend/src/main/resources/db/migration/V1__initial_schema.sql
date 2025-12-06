-- V1: Initial schema for ThermaFlow

-- Ingredients table
CREATE TABLE ingredients (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    viscosity INT NOT NULL,
    scent_profile VARCHAR(50) NOT NULL,
    stock_level INT NOT NULL,
    cost_per_ml DECIMAL(10, 2) NOT NULL,
    description VARCHAR(1000)
);

-- Infusion recipes table
CREATE TABLE infusion_recipes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(2000),
    theme VARCHAR(500)
);

-- Infusion steps table
CREATE TABLE infusion_steps (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    recipe_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    duration_seconds INT NOT NULL,
    heat_intensity INT NOT NULL,
    scent_dosage_ml INT NOT NULL,
    ingredient_id BIGINT,
    music_track_id VARCHAR(255),
    lighting_scene VARCHAR(255),
    step_order INT NOT NULL,
    FOREIGN KEY (recipe_id) REFERENCES infusion_recipes(id) ON DELETE CASCADE,
    FOREIGN KEY (ingredient_id) REFERENCES ingredients(id)
);

-- Sauna rooms table
CREATE TABLE sauna_rooms (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    capacity INT NOT NULL,
    type VARCHAR(50) NOT NULL,
    has_sound_system BOOLEAN NOT NULL,
    required_cool_down_min INT NOT NULL,
    description VARCHAR(1000),
    location VARCHAR(255)
);

-- Employees table
CREATE TABLE employees (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE,
    certification_level INT NOT NULL,
    daily_max_infusions INT NOT NULL,
    active BOOLEAN DEFAULT TRUE
);

-- Employee skills table
CREATE TABLE employee_skills (
    employee_id BIGINT NOT NULL,
    skill VARCHAR(50) NOT NULL,
    FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE,
    PRIMARY KEY (employee_id, skill)
);

-- Shift plans table
CREATE TABLE shift_plans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    notes VARCHAR(500),
    FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE
);

-- Daily schedules table
CREATE TABLE daily_schedules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    date DATE NOT NULL UNIQUE,
    published BOOLEAN DEFAULT FALSE,
    notes VARCHAR(1000)
);

-- Infusion slots table
CREATE TABLE infusion_slots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    schedule_id BIGINT NOT NULL,
    room_id BIGINT NOT NULL,
    recipe_id BIGINT NOT NULL,
    employee_id BIGINT NOT NULL,
    start_time TIME NOT NULL,
    confirmed BOOLEAN DEFAULT FALSE,
    cancelled BOOLEAN DEFAULT FALSE,
    notes VARCHAR(500),
    FOREIGN KEY (schedule_id) REFERENCES daily_schedules(id) ON DELETE CASCADE,
    FOREIGN KEY (room_id) REFERENCES sauna_rooms(id),
    FOREIGN KEY (recipe_id) REFERENCES infusion_recipes(id),
    FOREIGN KEY (employee_id) REFERENCES employees(id)
);

-- Indexes for performance
CREATE INDEX idx_infusion_steps_recipe ON infusion_steps(recipe_id);
CREATE INDEX idx_shift_plans_employee_date ON shift_plans(employee_id, date);
CREATE INDEX idx_infusion_slots_schedule ON infusion_slots(schedule_id);
CREATE INDEX idx_infusion_slots_room_schedule ON infusion_slots(room_id, schedule_id);
CREATE INDEX idx_infusion_slots_employee_schedule ON infusion_slots(employee_id, schedule_id);
