package com.womenconcern.api.utils;


import com.womenconcern.api.project.dto.response.ProjectResponse;
import com.womenconcern.api.project.entity.Project;
import org.springframework.stereotype.Component;

@Component
public class ProjectMapper {

    public ProjectResponse toResponse(Project project) {
        if (project == null) {
            return null;
        }

        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .projectManagerId(project.getProjectManagerId())
                .totalBudget(project.getTotalBudget())
                .status(project.getStatus())
                .projectManagerApprovalStatus(project.getProjectManagerApprovalStatus())
                .financeApprovalStatus(project.getFinanceApprovalStatus())
                .executiveApprovalStatus(project.getExecutiveApprovalStatus())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }
}
