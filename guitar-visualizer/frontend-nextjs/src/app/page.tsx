'use client';

import { useState, useEffect } from 'react';
import styled from 'styled-components';
import { Fretboard } from '@/components/Fretboard';
import { Chord, ChordVariation, ChordQuality, ApiResponse } from '@/types';
import { Search, Music, Heart, Settings } from 'lucide-react';

const AppContainer = styled.div`
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
`;

const Header = styled.header`
  text-align: center;
  margin-bottom: 40px;
`;

const Title = styled.h1`
  color: white;
  font-size: 3rem;
  font-weight: 700;
  margin-bottom: 10px;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
`;

const Subtitle = styled.p`
  color: rgba(255, 255, 255, 0.9);
  font-size: 1.2rem;
  margin: 0;
`;

const SearchContainer = styled.div`
  max-width: 600px;
  margin: 0 auto 40px auto;
  display: flex;
  gap: 10px;
`;

const SearchInput = styled.input`
  flex: 1;
  padding: 15px 20px;
  border: none;
  border-radius: 25px;
  font-size: 1.1rem;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  outline: none;
  
  &:focus {
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
  }
`;

const SearchButton = styled.button`
  background: #2ecc71;
  color: white;
  border: none;
  border-radius: 50%;
  width: 50px;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  
  &:hover {
    background: #27ae60;
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.2);
  }
`;

const QualityFilters = styled.div`
  display: flex;
  justify-content: center;
  gap: 10px;
  margin-bottom: 40px;
  flex-wrap: wrap;
`;

const FilterButton = styled.button<{ $active: boolean }>`
  padding: 8px 16px;
  border: 2px solid ${props => props.$active ? '#2ecc71' : 'rgba(255, 255, 255, 0.3)'};
  background: ${props => props.$active ? '#2ecc71' : 'transparent'};
  color: white;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-weight: 500;
  
  &:hover {
    background: ${props => props.$active ? '#27ae60' : 'rgba(255, 255, 255, 0.1)'};
  }
`;

const ChordsGrid = styled.div`
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 30px;
  max-width: 1200px;
  margin: 0 auto;
`;

const LoadingMessage = styled.div`
  text-align: center;
  color: white;
  font-size: 1.2rem;
  margin: 40px 0;
`;

const ErrorMessage = styled.div`
  text-align: center;
  color: #e74c3c;
  background: rgba(255, 255, 255, 0.9);
  padding: 20px;
  border-radius: 10px;
  margin: 40px auto;
  max-width: 500px;
`;

const StatusIndicator = styled.div<{ $status: 'connecting' | 'connected' | 'error' }>`
  position: fixed;
  top: 20px;
  right: 20px;
  padding: 10px 15px;
  border-radius: 20px;
  color: white;
  font-size: 0.9rem;
  display: flex;
  align-items: center;
  gap: 8px;
  background: ${props => {
    switch (props.$status) {
      case 'connected': return '#2ecc71';
      case 'error': return '#e74c3c';
      default: return '#f39c12';
    }
  }};
`;

const qualities: ChordQuality[] = ['major', 'minor', 'dominant7', 'major7', 'minor7', 'suspended2', 'suspended4', 'diminished', 'augmented'];

export default function Home() {
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedQuality, setSelectedQuality] = useState<ChordQuality | null>(null);
  const [chords, setChords] = useState<Chord[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [backendStatus, setBackendStatus] = useState<'connecting' | 'connected' | 'error'>('connecting');

  // Check backend health on component mount
  useEffect(() => {
    checkBackendHealth();
  }, []);

  const checkBackendHealth = async () => {
    try {
      const response = await fetch('http://localhost:8080/health');
      if (response.ok) {
        setBackendStatus('connected');
      } else {
        setBackendStatus('error');
      }
    } catch (err) {
      setBackendStatus('error');
    }
  };

  const handleSearch = async () => {
    if (!searchTerm.trim()) return;
    
    setLoading(true);
    setError(null);
    
    try {
      let url = `http://localhost:8080/chords/${encodeURIComponent(searchTerm)}`;
      
      if (selectedQuality) {
        url += `/${selectedQuality}`;
      }
      
      const response = await fetch(url);
      
      if (!response.ok) {
        throw new Error(`Server error: ${response.status}`);
      }
      
      const data: ApiResponse<Chord[]> = await response.json();
      
      if (data.success) {
        setChords(data.data || []);
      } else {
        throw new Error(data.error || 'Failed to fetch chords');
      }
    } catch (err) {
      console.error('Search error:', err);
      setError(err instanceof Error ? err.message : 'Failed to search chords');
      setChords([]);
    } finally {
      setLoading(false);
    }
  };

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') {
      handleSearch();
    }
  };

  const toggleQuality = (quality: ChordQuality) => {
    setSelectedQuality(prev => 
      prev === quality ? null : quality
    );
  };

  const getQualityDisplayName = (quality: ChordQuality): string => {
    const names: Record<ChordQuality, string> = {
      major: 'Major',
      minor: 'Minor',
      dominant7: 'Dom7',
      major7: 'Maj7',
      minor7: 'Min7',
      suspended2: 'Sus2',
      suspended4: 'Sus4',
      diminished: 'Dim',
      augmented: 'Aug'
    };
    return names[quality];
  };

  return (
    <AppContainer>
      <StatusIndicator $status={backendStatus}>
        <Settings size={16} />
        {backendStatus === 'connected' ? 'Backend Connected' : 
         backendStatus === 'error' ? 'Backend Offline' : 'Connecting...'}
      </StatusIndicator>

      <Header>
        <Title>
          <Music size={48} style={{ marginRight: '15px', verticalAlign: 'middle' }} />
          Harmony Hero
        </Title>
        <Subtitle>Discover and visualize guitar chords</Subtitle>
      </Header>

      <SearchContainer>
        <SearchInput
          type="text"
          placeholder="Enter chord root (e.g., C, F#, Bb)..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          onKeyPress={handleKeyPress}
        />
        <SearchButton onClick={handleSearch}>
          <Search size={24} />
        </SearchButton>
      </SearchContainer>

      <QualityFilters>
        {qualities.map(quality => (
          <FilterButton
            key={quality}
            $active={selectedQuality === quality}
            onClick={() => toggleQuality(quality)}
          >
            {getQualityDisplayName(quality)}
          </FilterButton>
        ))}
      </QualityFilters>

      {loading && <LoadingMessage>Searching for chords...</LoadingMessage>}
      
      {error && <ErrorMessage>{error}</ErrorMessage>}
      
      {chords.length > 0 && (
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
    </AppContainer>
  );
}
