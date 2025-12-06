package com.thermaflow.controller;

import com.thermaflow.dto.InfusionRecipeDTO;
import com.thermaflow.dto.RecipeMapper;
import com.thermaflow.model.InfusionRecipe;
import com.thermaflow.repository.InfusionRecipeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for recipe management.
 */
@RestController
@RequestMapping("/api/recipes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RecipeController {
    
    private final InfusionRecipeRepository recipeRepository;
    private final RecipeMapper recipeMapper;
    
    @GetMapping
    public List<InfusionRecipeDTO> getAllRecipes() {
        return recipeRepository.findAllWithSteps().stream()
                .map(recipeMapper::toDTO)
                .collect(Collectors.toList());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<InfusionRecipeDTO> getRecipeById(@PathVariable Long id) {
        return recipeRepository.findById(id)
                .map(recipeMapper::toDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public InfusionRecipeDTO createRecipe(@RequestBody InfusionRecipeDTO recipeDTO) {
        InfusionRecipe recipe = recipeMapper.toEntity(recipeDTO);
        InfusionRecipe saved = recipeRepository.save(recipe);
        return recipeMapper.toDTO(saved);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecipe(@PathVariable Long id) {
        if (recipeRepository.existsById(id)) {
            recipeRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
