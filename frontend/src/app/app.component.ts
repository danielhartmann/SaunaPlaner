import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { RecipeBuilderComponent } from './components/recipe-builder.component';

/**
 * Main application component - Standalone
 */
@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RecipeBuilderComponent],
  template: `
    <div class="app-container">
      <header class="app-header">
        <h1>🔥 ThermaFlow</h1>
        <p>High-end Sauna Infusion Management</p>
      </header>
      
      <main class="app-main">
        <app-recipe-builder></app-recipe-builder>
      </main>
      
      <footer class="app-footer">
        <p>&copy; 2024 ThermaFlow - Wellness Excellence</p>
      </footer>
    </div>
  `,
  styles: [`
    .app-container {
      min-height: 100vh;
      display: flex;
      flex-direction: column;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    }
    
    .app-header {
      background: rgba(255, 255, 255, 0.95);
      padding: 20px;
      text-align: center;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    }
    
    .app-header h1 {
      margin: 0;
      font-size: 2.5em;
      color: #333;
    }
    
    .app-header p {
      margin: 5px 0 0 0;
      color: #666;
    }
    
    .app-main {
      flex: 1;
      padding: 20px;
    }
    
    .app-footer {
      background: rgba(0, 0, 0, 0.8);
      color: white;
      text-align: center;
      padding: 15px;
    }
    
    .app-footer p {
      margin: 0;
    }
  `]
})
export class AppComponent {
  title = 'ThermaFlow';
}
