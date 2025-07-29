-- Guitar Chord Visualizer Database Initialization
-- This script sets up the initial database schema

-- Ensure we're using the correct database
\c guitar_chords;

-- Create tables for chord data
CREATE TABLE IF NOT EXISTS keys (
    id SERIAL PRIMARY KEY,
    name VARCHAR(10) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS qualities (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    display_name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS chords (
    id SERIAL PRIMARY KEY,
    key_id INTEGER NOT NULL REFERENCES keys(id) ON DELETE CASCADE,
    quality_id INTEGER NOT NULL REFERENCES qualities(id) ON DELETE CASCADE,
    representation VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(key_id, quality_id)
);

CREATE TABLE IF NOT EXISTS variations (
    id SERIAL PRIMARY KEY,
    chord_id INTEGER NOT NULL REFERENCES chords(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    diagram TEXT NOT NULL,
    difficulty_level INTEGER DEFAULT 1 CHECK (difficulty_level >= 1 AND difficulty_level <= 5),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_chords_key_quality ON chords(key_id, quality_id);
CREATE INDEX IF NOT EXISTS idx_variations_chord_id ON variations(chord_id);
CREATE INDEX IF NOT EXISTS idx_keys_name ON keys(name);
CREATE INDEX IF NOT EXISTS idx_qualities_name ON qualities(name);

-- Insert basic musical keys
INSERT INTO keys (name) VALUES 
    ('A'), ('A#'), ('B'), ('C'), ('C#'), ('D'), 
    ('D#'), ('E'), ('F'), ('F#'), ('G'), ('G#')
ON CONFLICT (name) DO NOTHING;

-- Insert common chord qualities
INSERT INTO qualities (name, display_name) VALUES 
    ('major', 'Major'),
    ('minor', 'Minor'),
    ('dominant7', 'Dominant 7th'),
    ('major7', 'Major 7th'),
    ('minor7', 'Minor 7th'),
    ('diminished', 'Diminished'),
    ('augmented', 'Augmented'),
    ('suspended4', 'Suspended 4th'),
    ('suspended2', 'Suspended 2nd'),
    ('add9', 'Add 9'),
    ('major6', 'Major 6th'),
    ('minor6', 'Minor 6th'),
    ('dominant9', 'Dominant 9th'),
    ('dominant11', 'Dominant 11th'),
    ('dominant13', 'Dominant 13th'),
    ('half-diminished', 'Half Diminished'),
    ('minor7b5', 'Minor 7♭5'),
    ('maj9', 'Major 9th'),
    ('sus2', 'Suspended 2nd'),
    ('sus4', 'Suspended 4th'),
    ('6', '6th'),
    ('m6', 'Minor 6th'),
    ('9', '9th'),
    ('13', '13th')
ON CONFLICT (name) DO NOTHING;

-- Create a view for easy chord querying
CREATE OR REPLACE VIEW chord_details AS
SELECT 
    k.name as key_name,
    q.name as quality_name,
    q.display_name as quality_display,
    c.representation,
    v.name as variation_name,
    v.diagram,
    v.difficulty_level,
    c.id as chord_id,
    v.id as variation_id
FROM chords c
JOIN keys k ON c.key_id = k.id
JOIN qualities q ON c.quality_id = q.id
JOIN variations v ON c.id = v.chord_id
ORDER BY k.name, q.name, v.name;

-- Grant permissions (if needed for specific users)
-- GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO admin;
-- GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO admin;

\echo 'Database schema initialized successfully!'
