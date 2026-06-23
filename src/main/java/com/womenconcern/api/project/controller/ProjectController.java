package com.womenconcern.api.project.controller;

import com.womenconcern.api.auth.dto.RejectProjectRequest;
import com.womenconcern.api.project.dto.request.*;
import com.womenconcern.api.project.dto.response.ProjectResponse;
import com.womenconcern.api.project.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@Tag(name = "Projects", description = "Project management endpoints")
@SecurityRequirement(name = "bearer-jwt")
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    @Operation(summary = "Create a new project")
    public ResponseEntity<ProjectResponse> createProject(
            @Valid @RequestBody CreateProjectRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID projectManagerId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(projectService.createProject(request, projectManagerId));
    }

    @GetMapping("/{projectId}")
    @Operation(summary = "Get project by ID")
    public ResponseEntity<ProjectResponse> getProject(@PathVariable UUID projectId) {
        return ResponseEntity.ok(projectService.getProjectById(projectId));
    }

    @GetMapping
    @Operation(summary = "Get all projects")
    public ResponseEntity<List<ProjectResponse>> getAllProjects() {
        return ResponseEntity.ok(projectService.getAllProjects());
    }

    @GetMapping("/my-projects")
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    @Operation(summary = "Get projects created by the current PM")
    public ResponseEntity<List<ProjectResponse>> getMyProjects(@AuthenticationPrincipal Jwt jwt) {
        UUID projectManagerId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(projectService.getProjectsByProjectManager(projectManagerId));
    }

    @PutMapping("/{projectId}")
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    @Operation(summary = "Update project (DRAFT only)")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable UUID projectId,
            @Valid @RequestBody CreateProjectRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID projectManagerId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(projectService.updateProject(projectId, request, projectManagerId));
    }

    @DeleteMapping("/{projectId}")
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    @Operation(summary = "Delete project (DRAFT only)")
    public ResponseEntity<Void> deleteProject(
            @PathVariable UUID projectId,
            @AuthenticationPrincipal Jwt jwt) {
        UUID projectManagerId = UUID.fromString(jwt.getSubject());
        projectService.deleteProject(projectId, projectManagerId);
        return ResponseEntity.noContent().build();
    }


    @PostMapping("/{projectId}/submit-for-finance-review")
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    @Operation(summary = "Submit Finance-ready project for Finance review")
    public ResponseEntity<ProjectResponse> submitForFinanceReview(
            @PathVariable UUID projectId,
            @AuthenticationPrincipal Jwt jwt) {
        UUID projectManagerId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(
                projectService.submitProjectForFinanceReview(projectId, projectManagerId));
    }

    @PostMapping("/{projectId}/approve-by-finance")
    @PreAuthorize("hasRole('FINANCE')")
    @Operation(summary = "Finance team approves budget")
    public ResponseEntity<ProjectResponse> approveByFinance(
            @PathVariable UUID projectId,
            @Valid @RequestBody ApproveProjectRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID financeId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(
                projectService.approveProjectByFinance(projectId, request.getApprovalNotes(), financeId));
    }

    @PostMapping("/{projectId}/reject-by-finance")
    @PreAuthorize("hasRole('FINANCE')")
    @Operation(summary = "Finance team rejects budget")
    public ResponseEntity<ProjectResponse> rejectByFinance(
            @PathVariable UUID projectId,
            @Valid @RequestBody RejectProjectRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID financeId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(
                projectService.rejectProjectByFinance(projectId, request.getRejectionReason(), financeId));
    }

    @PostMapping("/{projectId}/submit-for-executive-review")
    @PreAuthorize("hasRole('FINANCE')")
    @Operation(summary = "Finance submits to Executive Director after budget approval")
    public ResponseEntity<ProjectResponse> submitForExecutiveReview(
            @PathVariable UUID projectId,
            @AuthenticationPrincipal Jwt jwt) {
        UUID financeId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(
                projectService.submitProjectForExecutiveReview(projectId, financeId));
    }

    @PostMapping("/{projectId}/approve-by-executive")
    @PreAuthorize("hasRole('EXECUTIVE_DIRECTOR')")
    @Operation(summary = "Executive Director gives final approval")
    public ResponseEntity<ProjectResponse> approveByExecutive(
            @PathVariable UUID projectId,
            @Valid @RequestBody ApproveProjectRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID edId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(
                projectService.approveProjectByExecutive(projectId, request.getApprovalNotes(), edId));
    }

    @PostMapping("/{projectId}/reject-by-executive")
    @PreAuthorize("hasRole('EXECUTIVE_DIRECTOR')")
    @Operation(summary = "Executive Director rejects project")
    public ResponseEntity<ProjectResponse> rejectByExecutive(
            @PathVariable UUID projectId,
            @Valid @RequestBody RejectProjectRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID edId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(
                projectService.rejectProjectByExecutive(projectId, request.getRejectionReason(), edId));
    }

    @PostMapping("/{projectId}/start-implementation")
    @PreAuthorize("hasRole('EXECUTIVE_DIRECTOR')")
    @Operation(summary = "Move approved project into implementation phase")
    public ResponseEntity<ProjectResponse> startImplementation(
            @PathVariable UUID projectId,
            @AuthenticationPrincipal Jwt jwt) {
        UUID edId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(projectService.startImplementation(projectId, edId));
    }
}