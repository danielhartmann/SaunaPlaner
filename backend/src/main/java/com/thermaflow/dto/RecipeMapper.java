package com.thermaflow.dto;

import com.thermaflow.model.InfusionRecipe;
import com.thermaflow.model.InfusionStep;
import org.mapstruct.*;

/**
 * MapStruct mapper for Recipe and Step entities to DTOs.
 */
@Mapper(componentModel = "spring")
public interface RecipeMapper {
    
    @Mapping(target = "totalDuration", expression = "java(recipe.calculateTotalDuration())")
    @Mapping(target = "totalCost", expression = "java(recipe.calculateTotalCost())")
    InfusionRecipeDTO toDTO(InfusionRecipe recipe);
    
    @Mapping(target = "steps", ignore = true)
    InfusionRecipe toEntity(InfusionRecipeDTO dto);
    
    @Mapping(source = "ingredient.id", target = "ingredientId")
    @Mapping(source = "ingredient.name", target = "ingredientName")
    InfusionStepDTO toDTO(InfusionStep step);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "recipe", ignore = true)
    @Mapping(target = "ingredient", ignore = true)
    InfusionStep toEntity(InfusionStepDTO dto);
}
