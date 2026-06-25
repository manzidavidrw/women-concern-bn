package com.womenconcern.api.utils;


import com.womenconcern.api.project.dto.response.TaskResponse;
import com.womenconcern.api.project.entity.Task;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {

    public TaskResponse toResponse(Task task) {
        if (task == null) {
            return null;
        }

        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .costEstimate(task.getCostEstimate())
                .status(task.getStatus())
                .fieldOfficerId(task.getFieldOfficerId())
                .projectManagerApprovalStatus(task.getProjectManagerApprovalStatus())
                .financeApprovalStatus(task.getFinanceApprovalStatus())
                .executiveApprovalStatus(task.getExecutiveApprovalStatus())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
}