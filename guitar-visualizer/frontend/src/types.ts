export interface ChordVariation {
  name: string;
  diagram: string;
  difficultyLevel?: number;
}

export interface Chord {
  key: string;
  quality: string;
  representation: string;
  variations: ChordVariation[];
}

export interface ChordQuality {
  name: string;
  displayName: string;
}

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
