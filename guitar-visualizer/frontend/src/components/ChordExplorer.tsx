import React, { useState, useEffect } from 'react';
import styled from 'styled-components';
import { Chord, ChordQuality } from '../types';
import chordService from '../services/chordService';
import { Fretboard } from './Fretboard';

const Container = styled.div`
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
`;

const Header = styled.header`
  text-align: center;
  margin-bottom: 40px;
`;

const Title = styled.h1`
  color: #2c3e50;
  font-size: 2.5rem;
  margin-bottom: 10px;
`;

const Subtitle = styled.p`
  color: #7f8c8d;
  font-size: 1.1rem;
`;

const Controls = styled.div`
  display: flex;
  gap: 20px;
  margin-bottom: 40px;
  justify-content: center;
  flex-wrap: wrap;
`;

const Select = styled.select`
  padding: 10px 15px;
  border: 2px solid #bdc3c7;
  border-radius: 8px;
  font-size: 16px;
  background: white;
  cursor: pointer;
  
  &:focus {
    outline: none;
    border-color: #3498db;
  }
`;

const ChordsGrid = styled.div`
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 30px;
  margin-bottom: 40px;
`;

const LoadingSpinner = styled.div`
  display: flex;
  justify-content: center;
  align-items: center;
  height: 200px;
  font-size: 1.1rem;
  color: #7f8c8d;
`;

const ErrorMessage = styled.div`
  background: #ffebee;
  color: #c62828;
  padding: 15px;
  border-radius: 8px;
  text-align: center;
  margin: 20px 0;
`;

const keys = ['A', 'A#', 'B', 'C', 'C#', 'D', 'D#', 'E', 'F', 'F#', 'G', 'G#'];

export const ChordExplorer: React.FC = () => {
  const [selectedKey, setSelectedKey] = useState<string>('C');
  const [selectedQuality, setSelectedQuality] = useState<string>('');
  const [qualities, setQualities] = useState<ChordQuality[]>([]);
  const [chords, setChords] = useState<Chord[]>([]);
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string>('');

  // Load available qualities on component mount
  useEffect(() => {
    const loadQualities = async () => {
      try {
        const qualitiesData = await chordService.getQualities();
        setQualities(qualitiesData);
      } catch (err) {
        setError('Failed to load chord qualities');
        console.error('Error loading qualities:', err);
      }
    };

    loadQualities();
  }, []);

  // Load chords when key or quality changes
  useEffect(() => {
    const loadChords = async () => {
      if (!selectedKey) return;

      setLoading(true);
      setError('');

      try {
        if (selectedQuality) {
          // Load specific chord
          const chord = await chordService.getChord(selectedKey, selectedQuality);
          setChords([chord]);
        } else {
          // Load all chords for the key
          const chordsData = await chordService.getChordsByKey(selectedKey);
          setChords(chordsData);
        }
      } catch (err: any) {
        setError(err.message || 'Failed to load chords');
        setChords([]);
      } finally {
        setLoading(false);
      }
    };

    loadChords();
  }, [selectedKey, selectedQuality]);

  const handleKeyChange = (event: React.ChangeEvent<HTMLSelectElement>) => {
    setSelectedKey(event.target.value);
  };

  const handleQualityChange = (event: React.ChangeEvent<HTMLSelectElement>) => {
    setSelectedQuality(event.target.value);
  };

  return (
    <Container>
      <Header>
        <Title>🎸 Guitar Chord Visualizer</Title>
        <Subtitle>Explore guitar chord shapes and fingerings</Subtitle>
      </Header>

      <Controls>
        <div>
          <label htmlFor="key-select">Key: </label>
          <Select
            id="key-select"
            value={selectedKey}
            onChange={handleKeyChange}
          >
            {keys.map(key => (
              <option key={key} value={key}>
                {key}
              </option>
            ))}
          </Select>
        </div>

        <div>
          <label htmlFor="quality-select">Quality: </label>
          <Select
            id="quality-select"
            value={selectedQuality}
            onChange={handleQualityChange}
          >
            <option value="">All Qualities</option>
            {qualities.map(quality => (
              <option key={quality.name} value={quality.name}>
                {quality.displayName}
              </option>
            ))}
          </Select>
        </div>
      </Controls>

      {error && <ErrorMessage>{error}</ErrorMessage>}

      {loading && <LoadingSpinner>Loading chords...</LoadingSpinner>}

      {!loading && chords.length > 0 && (
        <ChordsGrid>
          {chords.map(chord => 
            chord.variations.map((variation, index) => (
              <Fretboard
                key={`${chord.key}-${chord.quality}-${index}`}
                variation={variation}
              />
            ))
          )}
        </ChordsGrid>
      )}

      {!loading && chords.length === 0 && !error && selectedKey && (
        <LoadingSpinner>
          No chords found for {selectedKey} {selectedQuality ? qualities.find(q => q.name === selectedQuality)?.displayName : ''}
        </LoadingSpinner>
      )}
    </Container>
  );
};
