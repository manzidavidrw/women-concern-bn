package com.womenconcern.api.project.controller;

import com.womenconcern.api.project.dto.request.CreateActivityRequest;
import com.womenconcern.api.project.dto.request.UpdateActivityRequest;
import com.womenconcern.api.project.dto.response.ActivityResponse;
import com.womenconcern.api.project.service.ActivityService;
import com.womenconcern.api.utils.ApiResponse;
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
@SecurityRequirement(name = "bearerAuth")
public class ActivityController {

    private final ActivityService activityService;

    @PostMapping
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    @Operation(
            summary = "Create activities for a result",
            description = "Create one or more activities under a result in a single request"
    )
    public ResponseEntity<ApiResponse<List<ActivityResponse>>> createActivities(
            @PathVariable UUID resultId,
            @Valid @RequestBody CreateActivityRequest request) {

        List<ActivityResponse> activities = activityService.createActivities(resultId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Activities created successfully", activities));
    }

    @GetMapping("/{activityId}")
    @PreAuthorize("hasRole('PROJECT_MANAGER') or hasRole('FINANCE') or hasRole('EXECUTIVE_DIRECTOR') or hasRole('FIELD_OFFICER')")
    @Operation(summary = "Get activity by ID")
    public ResponseEntity<ApiResponse<ActivityResponse>> getActivity(
            @PathVariable UUID resultId,
            @PathVariable UUID activityId) {

        return ResponseEntity.ok(new ApiResponse<>(true, "Activity retrieved successfully", activityService.getActivityById(activityId)));
    }

    @GetMapping
    @PreAuthorize("hasRole('PROJECT_MANAGER') or hasRole('FINANCE') or hasRole('EXECUTIVE_DIRECTOR') or hasRole('FIELD_OFFICER')")
    @Operation(summary = "Get all activities for a result")
    public ResponseEntity<ApiResponse<List<ActivityResponse>>> getActivitiesByResult(
            @PathVariable UUID resultId) {

        return ResponseEntity.ok(new ApiResponse<>(true, "Activities retrieved successfully", activityService.getActivitiesByResult(resultId)));
    }

    @PutMapping("/{activityId}")
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    @Operation(summary = "Update a single activity")
    public ResponseEntity<ApiResponse<ActivityResponse>> updateActivity(
            @PathVariable UUID resultId,
            @PathVariable UUID activityId,
            @Valid @RequestBody UpdateActivityRequest request) {

        return ResponseEntity.ok(new ApiResponse<>(true, "Activity updated successfully", activityService.updateActivity(activityId, request)));
    }

    @DeleteMapping("/{activityId}")
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    @Operation(summary = "Delete activity")
    public ResponseEntity<ApiResponse<Void>> deleteActivity(
            @PathVariable UUID resultId,
            @PathVariable UUID activityId) {

        activityService.deleteActivity(activityId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Activity deleted successfully", null));
    }

    @PostMapping("/{activityId}/assign-field-officer/{fieldOfficerId}")
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    @Operation(
            summary = "Assign a field officer to an activity",
            description = "Assigns a field officer and sends them an email notification. Project must be in IMPLEMENTATION status."
    )
    public ResponseEntity<ApiResponse<ActivityResponse>> assignFieldOfficer(
            @PathVariable UUID resultId,
            @PathVariable UUID activityId,
            @PathVariable UUID fieldOfficerId) {

        return ResponseEntity.ok(new ApiResponse<>(true, "Field officer assigned successfully", activityService.assignFieldOfficer(activityId, fieldOfficerId)));
    }

    @DeleteMapping("/{activityId}/unassign-field-officer")
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    @Operation(
            summary = "Unassign field officer from an activity",
            description = "Removes the assigned field officer and sends them an email notification."
    )
    public ResponseEntity<ApiResponse<ActivityResponse>> unassignFieldOfficer(
            @PathVariable UUID resultId,
            @PathVariable UUID activityId) {

        return ResponseEntity.ok(new ApiResponse<>(true, "Field officer unassigned successfully", activityService.unassignFieldOfficer(activityId)));
    }

    @GetMapping("/by-field-officer/{fieldOfficerId}")
    @PreAuthorize("hasRole('PROJECT_MANAGER') or hasRole('FIELD_OFFICER')")
    @Operation(summary = "Get all activities assigned to a field officer")
    public ResponseEntity<ApiResponse<List<ActivityResponse>>> getActivitiesByFieldOfficer(
            @PathVariable UUID resultId,
            @PathVariable UUID fieldOfficerId) {

        return ResponseEntity.ok(new ApiResponse<>(true, "Activities retrieved successfully", activityService.getActivitiesByFieldOfficer(fieldOfficerId)));
    }
}