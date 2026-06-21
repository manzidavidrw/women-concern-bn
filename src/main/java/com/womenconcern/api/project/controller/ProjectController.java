package com.womenconcern.api.project.controller;

import com.womenconcern.api.auth.dto.RejectProjectRequest;
import com.womenconcern.api.auth.entity.User;
import com.womenconcern.api.project.dto.request.*;
import com.womenconcern.api.project.dto.response.ProjectResponse;
import com.womenconcern.api.project.service.ProjectService;
import com.womenconcern.api.project.service.impl.ProjectExportService;
import com.womenconcern.api.utils.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@Tag(name = "Projects", description = "Project management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ProjectController {

    private final ProjectService projectService;
    private final ProjectExportService projectExportService;


    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('EXECUTIVE_DIRECTOR') or hasRole('PROJECT_MANAGER')")
    @Operation(summary = "Create a new project")
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(
            @Valid @RequestBody CreateProjectRequest request,
            Authentication auth) {

        UUID projectManagerId = ((User) auth.getPrincipal()).getId();
        ProjectResponse project = projectService.createProject(request, projectManagerId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Project created successfully", project));
    }

    @GetMapping("/my-projects")
    @PreAuthorize("hasRole('ADMIN') or hasRole('PROJECT_MANAGER')")
    @Operation(summary = "Get projects created by the current PM")
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getMyProjects(Authentication auth) {

        UUID projectManagerId = ((User) auth.getPrincipal()).getId();
        return ResponseEntity.ok(new ApiResponse<>(true, "Projects retrieved successfully",
                projectService.getProjectsByProjectManager(projectManagerId)));
    }

    @PutMapping("/{projectId}")
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    @Operation(summary = "Update project (DRAFT only)")
    public ResponseEntity<ApiResponse<ProjectResponse>> updateProject(
            @PathVariable UUID projectId,
            @Valid @RequestBody CreateProjectRequest request,
            Authentication auth) {

        UUID projectManagerId = ((User) auth.getPrincipal()).getId();
        return ResponseEntity.ok(new ApiResponse<>(true, "Project updated successfully",
                projectService.updateProject(projectId, request, projectManagerId)));
    }

    @DeleteMapping("/{projectId}")
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    @Operation(summary = "Delete project (DRAFT only)")
    public ResponseEntity<ApiResponse<Void>> deleteProject(
            @PathVariable UUID projectId,
            Authentication auth) {

        UUID projectManagerId = ((User) auth.getPrincipal()).getId();
        projectService.deleteProject(projectId, projectManagerId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Project deleted successfully", null));
    }

    @PostMapping("/{projectId}/submit-for-finance-review")
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    @Operation(summary = "Submit Finance-ready project for Finance review")
    public ResponseEntity<ApiResponse<ProjectResponse>> submitForFinanceReview(
            @PathVariable UUID projectId,
            Authentication auth) {

        UUID projectManagerId = ((User) auth.getPrincipal()).getId();
        return ResponseEntity.ok(new ApiResponse<>(true, "Project submitted for finance review",
                projectService.submitProjectForFinanceReview(projectId, projectManagerId)));
    }

    @PostMapping("/{projectId}/approve-by-finance")
    @PreAuthorize("hasRole('FINANCE')")
    @Operation(summary = "Finance team approves budget")
    public ResponseEntity<ApiResponse<ProjectResponse>> approveByFinance(
            @PathVariable UUID projectId,
            @Valid @RequestBody ApproveProjectRequest request,
            Authentication auth) {

        UUID financeId = ((User) auth.getPrincipal()).getId();
        return ResponseEntity.ok(new ApiResponse<>(true, "Project approved by finance",
                projectService.approveProjectByFinance(projectId, request.getApprovalNotes(), financeId)));
    }

    @PostMapping("/{projectId}/reject-by-finance")
    @PreAuthorize("hasRole('FINANCE')")
    @Operation(summary = "Finance team rejects budget")
    public ResponseEntity<ApiResponse<ProjectResponse>> rejectByFinance(
            @PathVariable UUID projectId,
            @Valid @RequestBody RejectProjectRequest request,
            Authentication auth) {

        UUID financeId = ((User) auth.getPrincipal()).getId();
        return ResponseEntity.ok(new ApiResponse<>(true, "Project rejected by finance",
                projectService.rejectProjectByFinance(projectId, request.getRejectionReason(), financeId)));
    }

    @PostMapping("/{projectId}/submit-for-executive-review")
    @PreAuthorize("hasRole('FINANCE')")
    @Operation(summary = "Finance submits to Executive Director after budget approval")
    public ResponseEntity<ApiResponse<ProjectResponse>> submitForExecutiveReview(
            @PathVariable UUID projectId,
            Authentication auth) {

        UUID financeId = ((User) auth.getPrincipal()).getId();
        return ResponseEntity.ok(new ApiResponse<>(true, "Project submitted for executive review",
                projectService.submitProjectForExecutiveReview(projectId, financeId)));
    }

    @PostMapping("/{projectId}/approve-by-executive")
    @PreAuthorize("hasRole('EXECUTIVE_DIRECTOR')")
    @Operation(summary = "Executive Director gives final approval")
    public ResponseEntity<ApiResponse<ProjectResponse>> approveByExecutive(
            @PathVariable UUID projectId,
            @Valid @RequestBody ApproveProjectRequest request,
            Authentication auth) {

        UUID edId = ((User) auth.getPrincipal()).getId();
        return ResponseEntity.ok(new ApiResponse<>(true, "Project approved by executive director",
                projectService.approveProjectByExecutive(projectId, request.getApprovalNotes(), edId)));
    }

    @PostMapping("/{projectId}/reject-by-executive")
    @PreAuthorize("hasRole('EXECUTIVE_DIRECTOR')")
    @Operation(summary = "Executive Director rejects project")
    public ResponseEntity<ApiResponse<ProjectResponse>> rejectByExecutive(
            @PathVariable UUID projectId,
            @Valid @RequestBody RejectProjectRequest request,
            Authentication auth) {

        UUID edId = ((User) auth.getPrincipal()).getId();
        return ResponseEntity.ok(new ApiResponse<>(true, "Project rejected by executive director",
                projectService.rejectProjectByExecutive(projectId, request.getRejectionReason(), edId)));
    }

    @PostMapping("/{projectId}/start-implementation")
    @PreAuthorize("hasRole('EXECUTIVE_DIRECTOR') or hasRole('PROJECT_MANAGER')")
    @Operation(summary = "Move approved project into implementation phase")
    public ResponseEntity<ApiResponse<ProjectResponse>> startImplementation(
            @PathVariable UUID projectId,
            Authentication auth) {

        UUID edId = ((User) auth.getPrincipal()).getId();
        return ResponseEntity.ok(new ApiResponse<>(true, "Project implementation started",
                projectService.startImplementation(projectId, edId)));
    }
    @GetMapping("/{projectId}/export/docx")
    @PreAuthorize("hasRole('PROJECT_MANAGER') or hasRole('FINANCE') or hasRole('EXECUTIVE_DIRECTOR') or hasRole('ADMIN')")
    @Operation(
            summary = "Export project as Word document",
            description = "Generates a full DOCX report including the Goal → Outcome → Result → Activity hierarchy, budget summary, and approval history."
    )
    public ResponseEntity<byte[]> exportProjectDocx(@PathVariable UUID projectId) {
        byte[] docx = projectExportService.exportProjectToDocx(projectId);

        String filename = "project-" + projectId + ".docx";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                .contentLength(docx.length)
                .body(docx);
    }
}