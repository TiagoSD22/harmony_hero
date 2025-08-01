-- Initial schema for guitar chord visualizer
-- This migration creates the core tables for storing chord data

-- Keys table (musical keys like A, B, C, etc.)
CREATE TABLE keys (
    id SERIAL PRIMARY KEY,
    name VARCHAR(10) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Chord qualities table (major, minor, dominant7, etc.)
CREATE TABLE qualities (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    display_name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Chords table (combination of key and quality)
CREATE TABLE chords (
    id SERIAL PRIMARY KEY,
    key_id INTEGER NOT NULL REFERENCES keys(id) ON DELETE CASCADE,
    quality_id INTEGER NOT NULL REFERENCES qualities(id) ON DELETE CASCADE,
    representation VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(key_id, quality_id)
);

-- Chord variations table (different fingering patterns for each chord)
CREATE TABLE variations (
    id SERIAL PRIMARY KEY,
    chord_id INTEGER NOT NULL REFERENCES chords(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    diagram TEXT NOT NULL,
    difficulty_level INTEGER DEFAULT 1 CHECK (difficulty_level >= 1 AND difficulty_level <= 5),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better query performance
CREATE INDEX idx_chords_key_quality ON chords(key_id, quality_id);
CREATE INDEX idx_variations_chord_id ON variations(chord_id);
CREATE INDEX idx_keys_name ON keys(name);
CREATE INDEX idx_qualities_name ON qualities(name);

-- Insert basic musical keys
INSERT INTO keys (name) VALUES 
    ('A'), ('A#'), ('B'), ('C'), ('C#'), ('D'), 
    ('D#'), ('E'), ('F'), ('F#'), ('G'), ('G#');

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
    ('minor7b5', 'Minor 7♭5');
