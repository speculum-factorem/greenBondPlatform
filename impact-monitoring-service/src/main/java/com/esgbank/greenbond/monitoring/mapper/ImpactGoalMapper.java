package com.esgbank.greenbond.monitoring.mapper;

import com.esgbank.greenbond.monitoring.dto.ImpactGoalRequest;
import com.esgbank.greenbond.monitoring.dto.ImpactGoalResponse;
import com.esgbank.greenbond.monitoring.model.ImpactGoal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ImpactGoalMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "goalId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "currentValue", ignore = true)
    @Mapping(target = "progressPercentage", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    ImpactGoal toEntity(ImpactGoalRequest request);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "goalId", source = "goalId")
    ImpactGoalResponse toResponse(ImpactGoal goal);
}