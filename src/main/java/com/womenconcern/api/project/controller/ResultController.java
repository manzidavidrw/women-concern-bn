package com.womenconcern.api.project.controller;

import com.womenconcern.api.project.dto.request.CreateResultRequest;
import com.womenconcern.api.project.dto.request.UpdateResultRequest;
import com.womenconcern.api.project.dto.response.ResultResponse;
import com.womenconcern.api.project.service.ResultService;
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
@RequestMapping("/api/v1/outcomes/{outcomeId}/results")
@RequiredArgsConstructor
@Tag(name = "Results", description = "Result management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ResultController {

    private final ResultService resultService;

    @PostMapping
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    @Operation(
            summary = "Create results for an outcome",
            description = "Create one or more results under an outcome in a single request"
    )
    public ResponseEntity<ApiResponse<List<ResultResponse>>> createResults(
            @PathVariable UUID outcomeId,
            @Valid @RequestBody CreateResultRequest request) {

        List<ResultResponse> results = resultService.createResult(outcomeId, request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Results created successfully", results));
    }

    @GetMapping("/{resultId}")
    @PreAuthorize("hasRole('PROJECT_MANAGER') or hasRole('FINANCE') or hasRole('EXECUTIVE_DIRECTOR')")
    @Operation(summary = "Get result by ID")
    public ResponseEntity<ApiResponse<ResultResponse>> getResult(
            @PathVariable UUID outcomeId,
            @PathVariable UUID resultId) {

        return ResponseEntity.ok(new ApiResponse<>(true, "Result retrieved successfully", resultService.getResultById(resultId)));
    }

    @GetMapping
    @PreAuthorize("hasRole('PROJECT_MANAGER') or hasRole('FINANCE') or hasRole('EXECUTIVE_DIRECTOR')")
    @Operation(summary = "Get all results for an outcome")
    public ResponseEntity<ApiResponse<List<ResultResponse>>> getResultsByOutcome(
            @PathVariable UUID outcomeId) {

        return ResponseEntity.ok(new ApiResponse<>(true, "Results retrieved successfully", resultService.getResultsByOutcome(outcomeId)));
    }

    @PutMapping("/{resultId}")
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    @Operation(summary = "Update result")
    public ResponseEntity<ApiResponse<ResultResponse>> updateResult(
            @PathVariable UUID outcomeId,
            @PathVariable UUID resultId,
            @Valid @RequestBody UpdateResultRequest request) {

        return ResponseEntity.ok(new ApiResponse<>(true, "Result updated successfully", resultService.updateResult(resultId, request)));
    }

    @DeleteMapping("/{resultId}")
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    @Operation(summary = "Delete result")
    public ResponseEntity<ApiResponse<Void>> deleteResult(
            @PathVariable UUID outcomeId,
            @PathVariable UUID resultId) {

        resultService.deleteResult(resultId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Result deleted successfully", null));
    }
}