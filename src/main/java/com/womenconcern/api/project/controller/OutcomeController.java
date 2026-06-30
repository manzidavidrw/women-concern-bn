package com.womenconcern.api.project.controller;

import com.womenconcern.api.project.dto.request.CreateOutcomeRequest;
import com.womenconcern.api.project.dto.request.UpdateOutcomeRequest;
import com.womenconcern.api.project.dto.response.OutcomeResponse;
import com.womenconcern.api.project.service.OutcomeService;
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
@RequestMapping("/api/v1/goals/{goalId}/outcomes")
@RequiredArgsConstructor
@Tag(name = "Outcomes", description = "Outcome management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class OutcomeController {

    private final OutcomeService outcomeService;

    @PostMapping
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    @Operation(
            summary = "Create outcomes for a goal",
            description = "Create one or more outcomes under a goal in a single request"
    )
    public ResponseEntity<ApiResponse<List<OutcomeResponse>>> createOutcomes(
            @PathVariable UUID goalId,
            @Valid @RequestBody CreateOutcomeRequest request) {

        List<OutcomeResponse> outcomes = outcomeService.createOutcomes(goalId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Outcomes created successfully", outcomes));
    }

    @GetMapping("/{outcomeId}")
    @PreAuthorize("hasRole('PROJECT_MANAGER') or hasRole('FINANCE') or hasRole('EXECUTIVE_DIRECTOR')")
    @Operation(summary = "Get outcome by ID")
    public ResponseEntity<ApiResponse<OutcomeResponse>> getOutcome(
            @PathVariable UUID goalId,
            @PathVariable UUID outcomeId) {

        return ResponseEntity.ok(new ApiResponse<>(true, "Outcome retrieved successfully", outcomeService.getOutcomeById(outcomeId)));
    }

    @GetMapping
    @PreAuthorize("hasRole('PROJECT_MANAGER') or hasRole('FINANCE') or hasRole('EXECUTIVE_DIRECTOR')")
    @Operation(summary = "Get all outcomes for a goal")
    public ResponseEntity<ApiResponse<List<OutcomeResponse>>> getOutcomesByGoal(
            @PathVariable UUID goalId) {

        return ResponseEntity.ok(new ApiResponse<>(true, "Outcomes retrieved successfully", outcomeService.getOutcomesByGoal(goalId)));
    }

    @PutMapping("/{outcomeId}")
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    @Operation(summary = "Update a single outcome")
    public ResponseEntity<ApiResponse<OutcomeResponse>> updateOutcome(
            @PathVariable UUID goalId,
            @PathVariable UUID outcomeId,
            @Valid @RequestBody UpdateOutcomeRequest request) {

        return ResponseEntity.ok(new ApiResponse<>(true, "Outcome updated successfully", outcomeService.updateOutcome(outcomeId, request)));
    }

    @DeleteMapping("/{outcomeId}")
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    @Operation(summary = "Delete outcome")
    public ResponseEntity<ApiResponse<Void>> deleteOutcome(
            @PathVariable UUID goalId,
            @PathVariable UUID outcomeId) {

        outcomeService.deleteOutcome(outcomeId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Outcome deleted successfully", null));
    }
}