import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { InfusionRecipe, Ingredient } from '../models/recipe.model';

/**
 * Service for recipe management API calls
 */
@Injectable({
  providedIn: 'root'
})
export class RecipeService {
  private readonly http = inject(HttpClient);
  private readonly apiUrl = 'http://localhost:8080/api';

  getRecipes(): Observable<InfusionRecipe[]> {
    return this.http.get<InfusionRecipe[]>(`${this.apiUrl}/recipes`);
  }

  getRecipe(id: number): Observable<InfusionRecipe> {
    return this.http.get<InfusionRecipe>(`${this.apiUrl}/recipes/${id}`);
  }

  createRecipe(recipe: InfusionRecipe): Observable<InfusionRecipe> {
    return this.http.post<InfusionRecipe>(`${this.apiUrl}/recipes`, recipe);
  }

  deleteRecipe(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/recipes/${id}`);
  }

  getIngredients(): Observable<Ingredient[]> {
    return this.http.get<Ingredient[]>(`${this.apiUrl}/ingredients`);
  }
}
