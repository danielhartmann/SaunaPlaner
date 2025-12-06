/**
 * TypeScript models for ThermaFlow application
 */

export enum ScentProfile {
  CITRUS = 'CITRUS',
  WOODY = 'WOODY',
  FLORAL = 'FLORAL',
  HERBAL = 'HERBAL'
}

export interface Ingredient {
  id?: number;
  name: string;
  viscosity: number;
  scentProfile: ScentProfile;
  stockLevel: number;
  costPerMl: number;
  description?: string;
}

export interface InfusionStep {
  id?: number;
  name: string;
  durationSeconds: number;
  heatIntensity: number;
  scentDosageMl: number;
  ingredientId?: number;
  ingredientName?: string;
  musicTrackId?: string;
  lightingScene?: string;
  stepOrder: number;
}

export interface InfusionRecipe {
  id?: number;
  name: string;
  description?: string;
  theme?: string;
  steps: InfusionStep[];
  totalDuration?: number;
  totalCost?: number;
}
