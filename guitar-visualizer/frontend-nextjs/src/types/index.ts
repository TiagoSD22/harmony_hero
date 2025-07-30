export interface ChordVariation {
  name: string;
  diagram: string;
  difficultyLevel?: number;
}

export interface Chord {
  key: string;
  quality: ChordQuality;
  representation: string;
  variations: ChordVariation[];
}

export type ChordQuality = 'major' | 'minor' | 'dominant7' | 'major7' | 'minor7' | 'suspended2' | 'suspended4' | 'diminished' | 'augmented';

export interface ApiResponse<T> {
  success: boolean;
  data?: T;
  error?: string;
}

export interface HealthStatus {
  status: string;
  timestamp: number;
  version: string;
}

export interface FretboardPosition {
  string: number; // 0-5 (low E to high E)
  fret: number;   // 0-24
  finger?: number; // 1-4 or 0 for open
  isRoot?: boolean;
  note?: string;
}
