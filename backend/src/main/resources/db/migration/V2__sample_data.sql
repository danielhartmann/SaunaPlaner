-- V2: Sample data for testing and demonstration

-- Insert sample ingredients
INSERT INTO ingredients (name, viscosity, scent_profile, stock_level, cost_per_ml, description) VALUES
('Eucalyptus Oil', 20, 'HERBAL', 5000, 0.15, 'Refreshing eucalyptus essential oil'),
('Lavender Oil', 25, 'FLORAL', 4500, 0.20, 'Calming lavender essential oil'),
('Pine Needle Extract', 30, 'WOODY', 6000, 0.12, 'Fresh pine forest scent'),
('Citrus Blend', 15, 'CITRUS', 5500, 0.18, 'Energizing citrus mix'),
('Sandalwood Oil', 35, 'WOODY', 3000, 0.30, 'Rich sandalwood aroma');

-- Insert sample sauna rooms
INSERT INTO sauna_rooms (name, capacity, type, has_sound_system, required_cool_down_min, description, location) VALUES
('Aurora Finnish Sauna', 12, 'FINNISH', true, 15, 'Traditional Finnish sauna with panoramic windows', 'Ground Floor'),
('Kelo Wood Sauna', 8, 'KELO', true, 20, 'Authentic Kelo pine wood sauna', 'Wellness Area'),
('Bio Sauna', 10, 'BIO', true, 10, 'Gentle bio sauna with mild temperatures', 'Upper Level');

-- Insert sample employees
INSERT INTO employees (first_name, last_name, email, certification_level, daily_max_infusions, active) VALUES
('Anna', 'Müller', 'anna.mueller@thermaflow.com', 4, 6, true),
('Max', 'Schmidt', 'max.schmidt@thermaflow.com', 5, 8, true),
('Sophie', 'Weber', 'sophie.weber@thermaflow.com', 3, 5, true);

-- Insert employee skills
INSERT INTO employee_skills (employee_id, skill) VALUES
(1, 'SINGING_BOWL'),
(1, 'AROMATHERAPY'),
(2, 'WENIK'),
(2, 'HIGH_HEAT'),
(2, 'SINGING_BOWL'),
(3, 'AROMATHERAPY'),
(3, 'MEDITATION');

-- Insert sample recipes
INSERT INTO infusion_recipes (name, description, theme) VALUES
('Nordic Aurora', 'A refreshing ritual inspired by northern lights', 'Nordic Experience'),
('Forest Awakening', 'Deep forest atmosphere with woody notes', 'Nature Connection'),
('Citrus Energy Boost', 'Energizing citrus-based infusion', 'Vitality');

-- Insert recipe steps
INSERT INTO infusion_steps (recipe_id, name, duration_seconds, heat_intensity, scent_dosage_ml, ingredient_id, music_track_id, lighting_scene, step_order) VALUES
-- Nordic Aurora recipe steps
(1, 'Round 1: Gentle Start', 300, 3, 50, 1, 'TRACK_001', 'DMX_BLUE_SOFT', 0),
(1, 'Round 2: Building Heat', 240, 6, 75, 4, 'TRACK_002', 'DMX_BLUE_MEDIUM', 1),
(1, 'Round 3: Peak Experience', 180, 8, 100, 1, 'TRACK_003', 'DMX_AURORA', 2),

-- Forest Awakening recipe steps
(2, 'Round 1: Forest Entry', 360, 4, 80, 3, 'TRACK_010', 'DMX_GREEN_SOFT', 0),
(2, 'Round 2: Deep Woods', 300, 7, 120, 5, 'TRACK_011', 'DMX_GREEN_DEEP', 1),

-- Citrus Energy Boost recipe steps
(3, 'Round 1: Wake Up', 240, 5, 60, 4, 'TRACK_020', 'DMX_ORANGE_BRIGHT', 0),
(3, 'Round 2: Energy Peak', 180, 7, 90, 4, 'TRACK_021', 'DMX_YELLOW_INTENSE', 1);
