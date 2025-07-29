import axios from 'axios';
import { ApiResponse, Chord, ChordQuality, HealthStatus } from '../types';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Response interceptor for error handling
api.interceptors.response.use(
  (response: any) => response,
  (error: any) => {
    console.error('API Error:', error);
    return Promise.reject(error);
  }
);

export const chordService = {
  // Get health status
  async getHealth(): Promise<HealthStatus> {
    const response = await api.get<ApiResponse<HealthStatus>>('/health');
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    throw new Error(response.data.error || 'Failed to get health status');
  },

  // Get all chord qualities
  async getQualities(): Promise<ChordQuality[]> {
    const response = await api.get<ApiResponse<ChordQuality[]>>('/chords/qualities');
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    throw new Error(response.data.error || 'Failed to fetch chord qualities');
  },

  // Get all chords for a specific key
  async getChordsByKey(key: string): Promise<Chord[]> {
    const response = await api.get<ApiResponse<Chord[]>>(`/chords/${key}`);
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    throw new Error(response.data.error || `Failed to fetch chords for key ${key}`);
  },

  // Get specific chord by key and quality
  async getChord(key: string, quality: string): Promise<Chord> {
    const response = await api.get<ApiResponse<Chord>>(`/chords/${key}/${quality}`);
    if (response.data.success && response.data.data) {
      return response.data.data;
    }
    throw new Error(response.data.error || `Failed to fetch ${quality} chord for key ${key}`);
  },
};

export default chordService;
