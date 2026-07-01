package com.womenconcern.api.project.controller;

import com.womenconcern.api.project.dto.request.CreateGoalRequest;
import com.womenconcern.api.project.dto.request.UpdateGoalRequest;
import com.womenconcern.api.project.dto.response.GoalResponse;
import com.womenconcern.api.project.service.GoalService;
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
@RequestMapping("/api/v1/projects/{projectId}/goals")
@RequiredArgsConstructor
@Tag(name = "Goals", description = "Goal management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class GoalController {

    private final GoalService goalService;

    @PostMapping
    @PreAuthorize("hasRole('PROJECT_MANAGER') or hasRole('ADMIN')")
    @Operation(
            summary = "Create project goals",
            description = "Only Project Manager can create one or more goals for a project"
    )
    public ResponseEntity<ApiResponse<List<GoalResponse>>> createGoals(
            @PathVariable UUID projectId,
            @Valid @RequestBody CreateGoalRequest request) {

        List<GoalResponse> goals = goalService.createGoals(projectId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Goals created successfully", goals));
    }

    @GetMapping("/{goalId}")
    @PreAuthorize("hasRole('PROJECT_MANAGER') or hasRole('FINANCE') or hasRole('EXECUTIVE_DIRECTOR')")
    @Operation(summary = "Get goal by ID")
    public ResponseEntity<ApiResponse<GoalResponse>> getGoal(
            @PathVariable UUID projectId,
            @PathVariable UUID goalId) {

        return ResponseEntity.ok(new ApiResponse<>(true, "Goal retrieved successfully", goalService.getGoalById(goalId)));
    }

    @GetMapping
    @PreAuthorize("hasRole('PROJECT_MANAGER') or hasRole('FINANCE') or hasRole('EXECUTIVE_DIRECTOR')")
    @Operation(summary = "Get all goals for a project")
    public ResponseEntity<ApiResponse<List<GoalResponse>>> getGoalsByProject(
            @PathVariable UUID projectId) {

        return ResponseEntity.ok(new ApiResponse<>(true, "Goals retrieved successfully", goalService.getGoalsByProject(projectId)));
    }

    @PutMapping("/{goalId}")
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    @Operation(summary = "Update a single goal")
    public ResponseEntity<ApiResponse<GoalResponse>> updateGoal(
            @PathVariable UUID projectId,
            @PathVariable UUID goalId,
            @Valid @RequestBody UpdateGoalRequest request) {

        return ResponseEntity.ok(new ApiResponse<>(true, "Goal updated successfully", goalService.updateGoal(goalId, request)));
    }

    @DeleteMapping("/{goalId}")
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    @Operation(summary = "Delete goal")
    public ResponseEntity<ApiResponse<Void>> deleteGoal(
            @PathVariable UUID projectId,
            @PathVariable UUID goalId) {

        goalService.deleteGoal(goalId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Goal deleted successfully", null));
    }
}