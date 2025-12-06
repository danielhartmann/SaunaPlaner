package com.thermaflow.controller;

import com.thermaflow.model.Ingredient;
import com.thermaflow.repository.IngredientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for ingredient management.
 */
@RestController
@RequestMapping("/api/ingredients")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class IngredientController {
    
    private final IngredientRepository ingredientRepository;
    
    @GetMapping
    public List<Ingredient> getAllIngredients() {
        return ingredientRepository.findAll();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Ingredient> getIngredientById(@PathVariable Long id) {
        return ingredientRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public Ingredient createIngredient(@RequestBody Ingredient ingredient) {
        return ingredientRepository.save(ingredient);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Ingredient> updateIngredient(@PathVariable Long id, @RequestBody Ingredient ingredient) {
        if (!ingredientRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        ingredient.setId(id);
        return ResponseEntity.ok(ingredientRepository.save(ingredient));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIngredient(@PathVariable Long id) {
        if (ingredientRepository.existsById(id)) {
            ingredientRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
