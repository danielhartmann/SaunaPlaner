import { Component, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSelectModule } from '@angular/material/select';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { InfusionRecipe, InfusionStep, Ingredient } from '../models/recipe.model';
import { RecipeService } from '../services/recipe.service';

/**
 * RecipeBuilderComponent - Angular 19 Standalone Component
 * 
 * Features:
 * - Uses Signals for state management (no RxJS Subjects)
 * - Dynamic form with Signals for adding/removing InfusionSteps
 * - Computed signals for automatic Total Duration and Total Cost calculation
 * - New control flow syntax (@if, @for)
 * - Angular Material UI components
 */
@Component({
  selector: 'app-recipe-builder',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule,
    MatIconModule,
    MatDividerModule
  ],
  template: `
    <div class="recipe-builder-container">
      <mat-card>
        <mat-card-header>
          <mat-card-title>Infusion Recipe Builder</mat-card-title>
          <mat-card-subtitle>Create and customize your sauna infusion experience</mat-card-subtitle>
        </mat-card-header>
        
        <mat-card-content>
          <!-- Recipe Basic Info -->
          <div class="recipe-basic-info">
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Recipe Name</mat-label>
              <input matInput [(ngModel)]="recipeName" placeholder="e.g., Nordic Aurora">
            </mat-form-field>
            
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Theme</mat-label>
              <input matInput [(ngModel)]="recipeTheme" placeholder="e.g., Nordic Experience">
            </mat-form-field>
            
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Description</mat-label>
              <textarea matInput [(ngModel)]="recipeDescription" rows="3" 
                        placeholder="Describe the experience..."></textarea>
            </mat-form-field>
          </div>
          
          <mat-divider></mat-divider>
          
          <!-- Infusion Steps -->
          <div class="steps-section">
            <div class="section-header">
              <h3>Infusion Steps</h3>
              <button mat-raised-button color="primary" (click)="addStep()">
                <mat-icon>add</mat-icon>
                Add Step
              </button>
            </div>
            
            <!-- Step Cards using @for -->
            @for (step of steps(); track step.stepOrder) {
              <mat-card class="step-card">
                <mat-card-header>
                  <mat-card-title>{{ step.name }}</mat-card-title>
                  <button mat-icon-button color="warn" (click)="removeStep(step.stepOrder)">
                    <mat-icon>delete</mat-icon>
                  </button>
                </mat-card-header>
                
                <mat-card-content>
                  <div class="step-form">
                    <mat-form-field appearance="outline">
                      <mat-label>Step Name</mat-label>
                      <input matInput [(ngModel)]="step.name" placeholder="e.g., Round 1">
                    </mat-form-field>
                    
                    <mat-form-field appearance="outline">
                      <mat-label>Duration (seconds)</mat-label>
                      <input matInput type="number" [(ngModel)]="step.durationSeconds" 
                             (ngModelChange)="onStepChange()" min="0">
                    </mat-form-field>
                    
                    <mat-form-field appearance="outline">
                      <mat-label>Heat Intensity (1-10)</mat-label>
                      <input matInput type="number" [(ngModel)]="step.heatIntensity" 
                             min="1" max="10">
                    </mat-form-field>
                    
                    <mat-form-field appearance="outline">
                      <mat-label>Scent Dosage (ml)</mat-label>
                      <input matInput type="number" [(ngModel)]="step.scentDosageMl" 
                             (ngModelChange)="onStepChange()" min="0">
                    </mat-form-field>
                    
                    <mat-form-field appearance="outline">
                      <mat-label>Ingredient</mat-label>
                      <mat-select [(ngModel)]="step.ingredientId" (ngModelChange)="onStepChange()">
                        @for (ingredient of availableIngredients(); track ingredient.id) {
                          <mat-option [value]="ingredient.id">
                            {{ ingredient.name }} ({{ ingredient.scentProfile }})
                          </mat-option>
                        }
                      </mat-select>
                    </mat-form-field>
                    
                    <mat-form-field appearance="outline">
                      <mat-label>Music Track ID</mat-label>
                      <input matInput [(ngModel)]="step.musicTrackId" placeholder="TRACK_001">
                    </mat-form-field>
                    
                    <mat-form-field appearance="outline">
                      <mat-label>Lighting Scene</mat-label>
                      <input matInput [(ngModel)]="step.lightingScene" placeholder="DMX_BLUE_SOFT">
                    </mat-form-field>
                  </div>
                </mat-card-content>
              </mat-card>
            }
            
            <!-- Empty state using @if -->
            @if (steps().length === 0) {
              <div class="empty-state">
                <mat-icon>info</mat-icon>
                <p>No steps added yet. Click "Add Step" to create your first infusion round.</p>
              </div>
            }
          </div>
          
          <mat-divider></mat-divider>
          
          <!-- Calculated Summary -->
          <div class="summary-section">
            <h3>Recipe Summary</h3>
            <div class="summary-grid">
              <div class="summary-item">
                <span class="summary-label">Total Duration:</span>
                <span class="summary-value">{{ totalDuration() }} seconds ({{ formatDuration(totalDuration()) }})</span>
              </div>
              <div class="summary-item">
                <span class="summary-label">Total Cost:</span>
                <span class="summary-value">{{ '$' + totalCost().toFixed(2) }}</span>
              </div>
              <div class="summary-item">
                <span class="summary-label">Number of Steps:</span>
                <span class="summary-value">{{ steps().length }}</span>
              </div>
            </div>
          </div>
          
          <div class="actions">
            <button mat-raised-button color="accent" (click)="saveRecipe()" 
                    [disabled]="!isValid()">
              <mat-icon>save</mat-icon>
              Save Recipe
            </button>
            <button mat-button (click)="resetForm()">
              <mat-icon>clear</mat-icon>
              Clear
            </button>
          </div>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .recipe-builder-container {
      padding: 20px;
      max-width: 1200px;
      margin: 0 auto;
    }
    
    .recipe-basic-info {
      display: flex;
      flex-direction: column;
      gap: 16px;
      margin-bottom: 20px;
    }
    
    .full-width {
      width: 100%;
    }
    
    .steps-section {
      margin: 20px 0;
    }
    
    .section-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
    }
    
    .step-card {
      margin-bottom: 16px;
      background: #f5f5f5;
    }
    
    .step-card mat-card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    
    .step-form {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 16px;
      margin-top: 16px;
    }
    
    .empty-state {
      text-align: center;
      padding: 40px;
      color: #666;
    }
    
    .empty-state mat-icon {
      font-size: 48px;
      width: 48px;
      height: 48px;
    }
    
    .summary-section {
      margin: 20px 0;
    }
    
    .summary-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
      gap: 16px;
      margin-top: 16px;
    }
    
    .summary-item {
      padding: 16px;
      background: #e3f2fd;
      border-radius: 8px;
      display: flex;
      flex-direction: column;
    }
    
    .summary-label {
      font-weight: 500;
      color: #1976d2;
      margin-bottom: 8px;
    }
    
    .summary-value {
      font-size: 1.2em;
      font-weight: bold;
      color: #333;
    }
    
    .actions {
      display: flex;
      gap: 16px;
      margin-top: 20px;
      justify-content: flex-end;
    }
    
    mat-divider {
      margin: 20px 0;
    }
  `]
})
export class RecipeBuilderComponent {
  // Signals for reactive state management
  steps = signal<InfusionStep[]>([]);
  availableIngredients = signal<Ingredient[]>([]);
  recipeName = '';
  recipeTheme = '';
  recipeDescription = '';
  
  // Computed signals for automatic calculation
  // Recalculates automatically when steps signal changes
  totalDuration = computed(() => {
    return this.steps().reduce((sum, step) => sum + (step.durationSeconds || 0), 0);
  });
  
  totalCost = computed(() => {
    return this.steps().reduce((sum, step) => {
      if (step.ingredientId && step.scentDosageMl) {
        const ingredient = this.availableIngredients().find(i => i.id === step.ingredientId);
        if (ingredient) {
          return sum + (ingredient.costPerMl * step.scentDosageMl);
        }
      }
      return sum;
    }, 0);
  });
  
  constructor(private recipeService: RecipeService) {
    // Load available ingredients
    this.recipeService.getIngredients().subscribe({
      next: (ingredients) => this.availableIngredients.set(ingredients),
      error: (err) => console.error('Error loading ingredients:', err)
    });
  }
  
  /**
   * Adds a new step to the recipe
   */
  addStep(): void {
    const newStep: InfusionStep = {
      name: `Round ${this.steps().length + 1}`,
      durationSeconds: 300,
      heatIntensity: 5,
      scentDosageMl: 50,
      stepOrder: this.steps().length
    };
    
    // Update the signal by creating a new array
    this.steps.update(steps => [...steps, newStep]);
  }
  
  /**
   * Removes a step from the recipe
   */
  removeStep(stepOrder: number): void {
    this.steps.update(steps => {
      const filtered = steps.filter(s => s.stepOrder !== stepOrder);
      // Reorder remaining steps
      return filtered.map((step, index) => ({ ...step, stepOrder: index }));
    });
  }
  
  /**
   * Called when any step property changes to trigger computed signals
   */
  onStepChange(): void {
    // Force signal update by creating a new array reference
    this.steps.update(steps => [...steps]);
  }
  
  /**
   * Validates the recipe form
   */
  isValid(): boolean {
    return this.recipeName.trim().length > 0 && this.steps().length > 0;
  }
  
  /**
   * Saves the recipe to the backend
   */
  saveRecipe(): void {
    if (!this.isValid()) {
      return;
    }
    
    const recipe: InfusionRecipe = {
      name: this.recipeName,
      theme: this.recipeTheme || undefined,
      description: this.recipeDescription || undefined,
      steps: this.steps()
    };
    
    this.recipeService.createRecipe(recipe).subscribe({
      next: (saved) => {
        console.log('Recipe saved successfully:', saved);
        // TODO: Replace with MatSnackBar for better UX
        // Example: this.snackBar.open('Recipe saved successfully!', 'Close', { duration: 3000 });
        alert(`Recipe "${saved.name}" saved successfully!\nTotal Duration: ${saved.totalDuration}s\nTotal Cost: $${saved.totalCost?.toFixed(2)}`);
        this.resetForm();
      },
      error: (err) => {
        console.error('Error saving recipe:', err);
        // TODO: Replace with MatSnackBar for better UX
        alert('Error saving recipe. Please try again.');
      }
    });
  }
  
  /**
   * Resets the form to initial state
   */
  resetForm(): void {
    this.recipeName = '';
    this.recipeTheme = '';
    this.recipeDescription = '';
    this.steps.set([]);
  }
  
  /**
   * Formats duration from seconds to minutes:seconds
   */
  formatDuration(seconds: number): string {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins}:${secs.toString().padStart(2, '0')}`;
  }
}
