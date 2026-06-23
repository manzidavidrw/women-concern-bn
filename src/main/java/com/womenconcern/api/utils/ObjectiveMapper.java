package com.womenconcern.api.utils;


import com.womenconcern.api.project.dto.response.ObjectiveResponse;
import com.womenconcern.api.project.entity.Objective;
import org.springframework.stereotype.Component;

@Component
public class ObjectiveMapper {

    public ObjectiveResponse toResponse(Objective objective) {
        if (objective == null) {
            return null;
        }

        return ObjectiveResponse.builder()
                .id(objective.getId())
                .title(objective.getTitle())
                .description(objective.getDescription())
                .totalBudget(objective.getTotalBudget())
                .createdAt(objective.getCreatedAt())
                .updatedAt(objective.getUpdatedAt())
                .build();
    }
}