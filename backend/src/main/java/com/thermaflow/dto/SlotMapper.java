package com.thermaflow.dto;

import com.thermaflow.model.InfusionSlot;
import org.mapstruct.*;

/**
 * MapStruct mapper for InfusionSlot entity to DTO.
 */
@Mapper(componentModel = "spring")
public interface SlotMapper {
    
    @Mapping(source = "schedule.id", target = "scheduleId")
    @Mapping(source = "room.id", target = "roomId")
    @Mapping(source = "room.name", target = "roomName")
    @Mapping(source = "recipe.id", target = "recipeId")
    @Mapping(source = "recipe.name", target = "recipeName")
    @Mapping(source = "recipe.theme", target = "recipeTheme")
    @Mapping(source = "employee.id", target = "employeeId")
    @Mapping(target = "employeeName", expression = "java(slot.getEmployee().getFullName())")
    @Mapping(target = "endTime", expression = "java(slot.getEndTime())")
    @Mapping(target = "averageHeatIntensity", expression = "java(slot.getAverageHeatIntensity())")
    InfusionSlotDTO toDTO(InfusionSlot slot);
}
