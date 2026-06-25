package com.womenconcern.api.utils;


import com.womenconcern.api.project.dto.response.OutcomeResponse;
import com.womenconcern.api.project.entity.Outcome;
import org.springframework.stereotype.Component;

@Component
public class OutcomeMapper {

    public OutcomeResponse toResponse(Outcome outcome) {
        if (outcome == null) {
            return null;
        }

        return OutcomeResponse.builder()
                .id(outcome.getId())
                .title(outcome.getTitle())
                .description(outcome.getDescription())
                .totalBudget(outcome.getTotalBudget())
                .createdAt(outcome.getCreatedAt())
                .updatedAt(outcome.getUpdatedAt())
                .build();
    }
}
