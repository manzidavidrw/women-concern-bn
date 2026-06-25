package com.womenconcern.api.project.service;


import com.womenconcern.api.project.dto.request.CreateProjectRequest;
import com.womenconcern.api.project.dto.response.ProjectResponse;
import java.util.List;
import java.util.UUID;

public interface ProjectService {
    ProjectResponse createProject(CreateProjectRequest request, UUID projectManagerId);

    ProjectResponse getProjectById(UUID projectId);

    List<ProjectResponse> getAllProjects();

    List<ProjectResponse> getProjectsByProjectManager(UUID projectManagerId);

    ProjectResponse updateProject(UUID projectId, CreateProjectRequest request, UUID projectManagerId);

    void deleteProject(UUID projectId, UUID projectManagerId);


    ProjectResponse submitProjectForFinanceReview(UUID projectId, UUID projectManagerId);

    ProjectResponse approveProjectByFinance(UUID projectId, String approvalNotes, UUID financeTeamMemberId);

    ProjectResponse rejectProjectByFinance(UUID projectId, String rejectionReason, UUID financeTeamMemberId);

    ProjectResponse submitProjectForExecutiveReview(UUID projectId, UUID financeTeamMemberId);

    ProjectResponse approveProjectByExecutive(UUID projectId, String approvalNotes, UUID executiveDirectorId);

    ProjectResponse rejectProjectByExecutive(UUID projectId, String rejectionReason, UUID executiveDirectorId);

    ProjectResponse startImplementation(UUID projectId, UUID executiveDirectorId);
}
