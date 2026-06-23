package com.womenconcern.api.project.controller;

import com.womenconcern.api.project.dto.request.CreateImpactRequest;
import com.womenconcern.api.project.dto.response.ImpactResponse;
import com.womenconcern.api.project.service.ImpactService;
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
@RequestMapping("/api/v1/objectives/{objectiveId}/impacts")
@RequiredArgsConstructor
@Tag(name = "Impacts", description = "Impact management endpoints")
@SecurityRequirement(name = "bearer-jwt")
public class ImpactController {

    private final ImpactService impactService;

    @PostMapping
    @PreAuthorize("hasRole('project-manager')")
    @Operation(summary = "Create a new impact")
    public ResponseEntity<ImpactResponse> createImpact(
            @PathVariable UUID objectiveId,
            @Valid @RequestBody CreateImpactRequest request) {
        ImpactResponse response = impactService.createImpact(objectiveId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{impactId}")
    @PreAuthorize("hasRole('PROJECT_MANAGER') or hasRole('FINANCE') or hasRole('EXECUTIVE_DIRECTOR')")
    @Operation(summary = "Get impact by ID")
    public ResponseEntity<ImpactResponse> getImpact(
            @PathVariable UUID objectiveId,
            @PathVariable UUID impactId) {
        ImpactResponse response = impactService.getImpactById(impactId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('PROJECT_MANAGER') or hasRole('FINANCE) or hasRole('EXECUTIVE_DIRECTOR')")
    @Operation(summary = "Get all impacts for an objective")
    public ResponseEntity<List<ImpactResponse>> getImpactsByObjective(
            @PathVariable UUID objectiveId) {
        List<ImpactResponse> response = impactService.getImpactsByObjective(objectiveId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{impactId}")
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    @Operation(summary = "Update impact")
    public ResponseEntity<ImpactResponse> updateImpact(
            @PathVariable UUID objectiveId,
            @PathVariable UUID impactId,
            @Valid @RequestBody CreateImpactRequest request) {
        ImpactResponse response = impactService.updateImpact(impactId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{impactId}")
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    @Operation(summary = "Delete impact")
    public ResponseEntity<Void> deleteImpact(
            @PathVariable UUID objectiveId,
            @PathVariable UUID impactId) {
        impactService.deleteImpact(impactId);
        return ResponseEntity.noContent().build();
    }
}