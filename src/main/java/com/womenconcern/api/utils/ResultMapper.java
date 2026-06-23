package com.womenconcern.api.utils;

import com.womenconcern.api.project.dto.response.ResultResponse;
import com.womenconcern.api.project.entity.Result;
import org.springframework.stereotype.Component;

@Component
public class ResultMapper {

    public ResultResponse toResponse(Result result) {
        if (result == null) {
            return null;
        }

        return ResultResponse.builder()
                .id(result.getId())
                .title(result.getTitle())
                .description(result.getDescription())
                .totalBudget(result.getTotalBudget())
                .createdAt(result.getCreatedAt())
                .updatedAt(result.getUpdatedAt())
                .build();
    }
}
