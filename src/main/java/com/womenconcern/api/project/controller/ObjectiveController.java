package com.womenconcern.api.project.controller;

import com.womenconcern.api.project.dto.request.CreateObjectiveRequest;
import com.womenconcern.api.project.dto.response.ObjectiveResponse;
import com.womenconcern.api.project.service.ObjectiveService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/projects/{projectId}/objectives")
@RequiredArgsConstructor
@Tag(name = "Objectives", description = "Objective management endpoints")
@SecurityRequirement(name = "bearer-jwt")
public class ObjectiveController {

    private final ObjectiveService objectiveService;

    @PostMapping
    @PreAuthorize("hasRole('project-manager')")
    @Operation(summary = "Create a new objective", description = "Only Project Manager can create objectives")
    public ResponseEntity<ObjectiveResponse> createObjective(
            @PathVariable UUID projectId,
            @Valid @RequestBody CreateObjectiveRequest request) {
        ObjectiveResponse response = objectiveService.createObjective(projectId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{objectiveId}")
    @PreAuthorize("hasRole('PROJECT_MANAGER') or hasRole('FINANCE') or hasRole('EXECUTIVE_DIRECTOR')")
    @Operation(summary = "Get objective by ID")
    public ResponseEntity<ObjectiveResponse> getObjective(
            @PathVariable UUID projectId,
            @PathVariable UUID objectiveId) {
        ObjectiveResponse response = objectiveService.getObjectiveById(objectiveId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('PROJECT_MANAGER') or hasRole('FINANCE') or hasRole('EXECUTIVE_DIRECTOR')")
    @Operation(summary = "Get all objectives for a project")
    public ResponseEntity<List<ObjectiveResponse>> getObjectivesByProject(
            @PathVariable UUID projectId) {
        List<ObjectiveResponse> response = objectiveService.getObjectivesByProject(projectId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{objectiveId}")
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    @Operation(summary = "Update objective")
    public ResponseEntity<ObjectiveResponse> updateObjective(
            @PathVariable UUID projectId,
            @PathVariable UUID objectiveId,
            @Valid @RequestBody CreateObjectiveRequest request) {
        ObjectiveResponse response = objectiveService.updateObjective(objectiveId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{objectiveId}")
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    @Operation(summary = "Delete objective")
    public ResponseEntity<Void> deleteObjective(
            @PathVariable UUID projectId,
            @PathVariable UUID objectiveId) {
        objectiveService.deleteObjective(objectiveId);
        return ResponseEntity.noContent().build();
    }
}