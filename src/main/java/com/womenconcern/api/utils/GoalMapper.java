package com.womenconcern.api.utils;


import com.womenconcern.api.project.dto.response.GoalResponse;
import com.womenconcern.api.project.entity.Goal;
import org.springframework.stereotype.Component;

@Component
public class GoalMapper {

    public GoalResponse toResponse(Goal goal) {
        if (goal == null) {
            return null;
        }

        return GoalResponse.builder()
                .id(goal.getId())
                .title(goal.getTitle())
                .description(goal.getDescription())
                .totalBudget(goal.getTotalBudget())
                .createdAt(goal.getCreatedAt())
                .updatedAt(goal.getUpdatedAt())
                .build();
    }
}