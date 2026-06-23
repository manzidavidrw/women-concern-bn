package com.womenconcern.api.utils;


import com.womenconcern.api.project.dto.response.ActivityResponse;
import com.womenconcern.api.project.entity.Activity;
import org.springframework.stereotype.Component;

@Component
public class ActivityMapper {

    public ActivityResponse toResponse(Activity activity) {
        if (activity == null) {
            return null;
        }

        return ActivityResponse.builder()
                .id(activity.getId())
                .title(activity.getTitle())
                .description(activity.getDescription())
                .costEstimate(activity.getCostEstimate())
                .totalBudget(activity.getTotalBudget())
                .fieldOfficerId(activity.getFieldOfficerId())
                .createdAt(activity.getCreatedAt())
                .updatedAt(activity.getUpdatedAt())
                .build();
    }
}