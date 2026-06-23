package com.womenconcern.api.utils;

import com.womenconcern.api.project.dto.response.ImpactResponse;
import com.womenconcern.api.project.entity.Impact;
import org.springframework.stereotype.Component;

@Component
public class ImpactMapper {

    public ImpactResponse toResponse(Impact impact) {
        if (impact == null) {
            return null;
        }

        return ImpactResponse.builder()
                .id(impact.getId())
                .title(impact.getTitle())
                .description(impact.getDescription())
                .totalBudget(impact.getTotalBudget())
                .createdAt(impact.getCreatedAt())
                .updatedAt(impact.getUpdatedAt())
                .build();
    }
}