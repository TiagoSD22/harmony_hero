import styled from 'styled-components';
import { ChordVariation, FretboardPosition } from '@/types';

interface FretboardProps {
  variation: ChordVariation;
  frets?: number;
}

const FretboardContainer = styled.div`
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
`;

const FretboardTitle = styled.h3`
  margin: 0 0 20px 0;
  color: #2c3e50;
  font-size: 1.2rem;
  font-weight: 600;
`;

const FretboardSvg = styled.svg`
  border: 2px solid #34495e;
  border-radius: 8px;
  background: linear-gradient(45deg, #ffeaa7, #fdcb6e);
`;

const StringLine = styled.line`
  stroke: #2d3436;
  stroke-width: 2;
`;

const FretLine = styled.line`
  stroke: #636e72;
  stroke-width: 1;
`;

const NutLine = styled.line`
  stroke: #2d3436;
  stroke-width: 4;
`;

const FingerDot = styled.circle<{ $isRoot?: boolean }>`
  fill: ${props => props.$isRoot ? '#e74c3c' : '#3498db'};
  stroke: #2c3e50;
  stroke-width: 2;
`;

const OpenString = styled.circle`
  fill: none;
  stroke: #27ae60;
  stroke-width: 3;
`;

const MutedString = styled.text`
  fill: #e74c3c;
  font-size: 16px;
  font-weight: bold;
  text-anchor: middle;
  dominant-baseline: middle;
`;

const StringLabel = styled.text`
  fill: #2c3e50;
  font-size: 12px;
  font-weight: 600;
  text-anchor: middle;
  dominant-baseline: middle;
`;

const FretNumber = styled.text`
  fill: #7f8c8d;
  font-size: 10px;
  text-anchor: middle;
  dominant-baseline: middle;
`;

// Parse diagram string like "e0, b1, g0, d2, a3, Ex" into fretboard positions
function parseDiagram(diagram: string): FretboardPosition[] {
  const positions: FretboardPosition[] = [];
  const parts = diagram.split(',').map(s => s.trim());
  
  // String mapping: e=1, b=2, g=3, d=4, a=5, E=6 (high to low)
  const stringMap: { [key: string]: number } = {
    'e': 1, 'b': 2, 'g': 3, 'd': 4, 'a': 5, 'E': 6
  };
  
  parts.forEach(part => {
    const stringChar = part.charAt(0).toLowerCase();
    const fretStr = part.slice(1);
    
    if (stringMap[stringChar] !== undefined) {
      const stringNum = stringMap[stringChar];
      let fret: number;
      
      if (fretStr.toLowerCase() === 'x') {
        fret = -1; // Muted
      } else {
        fret = parseInt(fretStr) || 0;
      }
      
      positions.push({
        string: stringNum,
        fret: fret,
        isRoot: stringChar === stringChar.toUpperCase() && fret > 0
      });
    }
  });
  
  return positions;
}

function Fretboard({ variation, frets = 12 }: FretboardProps) {
  const stringSpacing = 25;
  const fretSpacing = 30;
  const width = fretSpacing * (frets + 1) + 100;
  const height = stringSpacing * 6 + 80;
  
  // String names (from low E to high E)
  const stringNames = ['E', 'A', 'D', 'G', 'B', 'e'];
  
  // Parse the diagram string to get fret positions
  const positions = parseDiagram(variation.diagram);
  
  return (
    <FretboardContainer>
      <FretboardTitle>{variation.name}</FretboardTitle>
      <FretboardSvg width={width} height={height} viewBox={`0 0 ${width} ${height}`}>
        {/* Fret lines */}
        {Array.from({ length: frets + 1 }, (_, i) => (
          <FretLine
            key={`fret-${i}`}
            x1={50 + i * fretSpacing}
            y1={40}
            x2={50 + i * fretSpacing}
            y2={height - 40}
          />
        ))}
        
        {/* Nut (thicker line at fret 0) */}
        <NutLine
          x1={50}
          y1={40}
          x2={50}
          y2={height - 40}
        />
        
        {/* Strings */}
        {Array.from({ length: 6 }, (_, i) => (
          <StringLine
            key={`string-${i}`}
            x1={50}
            y1={40 + (i + 1) * stringSpacing}
            x2={width - 50}
            y2={40 + (i + 1) * stringSpacing}
          />
        ))}
        
        {/* String labels */}
        {stringNames.map((name, i) => (
          <StringLabel
            key={`label-${i}`}
            x={25}
            y={40 + (i + 1) * stringSpacing}
          >
            {name}
          </StringLabel>
        ))}
        
        {/* Fret numbers */}
        {[3, 5, 7, 9, 12].map(fretNum => (
          fretNum <= frets && (
            <FretNumber
              key={`fret-num-${fretNum}`}
              x={50 + fretNum * fretSpacing - fretSpacing / 2}
              y={height - 15}
            >
              {fretNum}
            </FretNumber>
          )
        ))}
        
        {/* Finger positions */}
        {positions.map((pos, index) => {
          const x = 50 + pos.fret * fretSpacing - fretSpacing / 2;
          const y = 40 + (6 - pos.string) * stringSpacing;
          
          if (pos.fret === 0) {
            // Open string
            return (
              <OpenString
                key={`pos-${index}`}
                cx={x}
                cy={y}
                r={8}
              />
            );
          } else if (pos.fret === -1) {
            // Muted string
            return (
              <MutedString
                key={`pos-${index}`}
                x={x}
                y={y}
              >
                ×
              </MutedString>
            );
          } else {
            // Fretted note
            return (
              <FingerDot
                key={`pos-${index}`}
                cx={x}
                cy={y}
                r={10}
                $isRoot={pos.isRoot}
              />
            );
          }
        })}
      </FretboardSvg>
    </FretboardContainer>
  );
}

export { Fretboard };
