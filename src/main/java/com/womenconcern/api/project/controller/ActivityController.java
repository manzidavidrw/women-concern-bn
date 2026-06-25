package com.womenconcern.api.project.controller;


import com.womenconcern.api.project.dto.request.CreateActivityRequest;
import com.womenconcern.api.project.dto.response.ActivityResponse;
import com.womenconcern.api.project.service.ActivityService;
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
@RequestMapping("/api/v1/results/{resultId}/activities")
@RequiredArgsConstructor
@Tag(name = "Activities", description = "Activity management endpoints")
@SecurityRequirement(name = "bearer-jwt")
public class ActivityController {

    private final ActivityService activityService;

    @PostMapping
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    @Operation(summary = "Create a new activity")
    public ResponseEntity<ActivityResponse> createActivity(
            @PathVariable UUID resultId,
            @Valid @RequestBody CreateActivityRequest request) {
        ActivityResponse response = activityService.createActivity(resultId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{activityId}")
    @PreAuthorize("hasRole('PROJECT_MANAGER') or hasRole('FINANCE') or hasRole('EXECUTIVE_DIRECTOR') or hasRole('field-officer')")
    @Operation(summary = "Get activity by ID")
    public ResponseEntity<ActivityResponse> getActivity(
            @PathVariable UUID resultId,
            @PathVariable UUID activityId) {
        ActivityResponse response = activityService.getActivityById(activityId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('PROJECT_MANAGER') or hasRole('FINANCE') or hasRole('EXECUTIVE_DIRECTOR') or hasRole('FIELD_OFFICER')")
    @Operation(summary = "Get all activities for a result")
    public ResponseEntity<List<ActivityResponse>> getActivitiesByResult(
            @PathVariable UUID resultId) {
        List<ActivityResponse> response = activityService.getActivitiesByResult(resultId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{activityId}")
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    @Operation(summary = "Update activity")
    public ResponseEntity<ActivityResponse> updateActivity(
            @PathVariable UUID resultId,
            @PathVariable UUID activityId,
            @Valid @RequestBody CreateActivityRequest request) {
        ActivityResponse response = activityService.updateActivity(activityId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{activityId}")
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    @Operation(summary = "Delete activity")
    public ResponseEntity<Void> deleteActivity(
            @PathVariable UUID resultId,
            @PathVariable UUID activityId) {
        activityService.deleteActivity(activityId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{activityId}/assign-field-officer/{fieldOfficerId}")
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    @Operation(summary = "Assign field officer to activity")
    public ResponseEntity<ActivityResponse> assignFieldOfficer(
            @PathVariable UUID resultId,
            @PathVariable UUID activityId,
            @PathVariable UUID fieldOfficerId) {
        ActivityResponse response = activityService.assignFieldOfficer(activityId, fieldOfficerId);
        return ResponseEntity.ok(response);
    }
}