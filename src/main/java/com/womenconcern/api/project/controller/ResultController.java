package com.womenconcern.api.project.controller;


import com.womenconcern.api.project.dto.request.CreateResultRequest;
import com.womenconcern.api.project.dto.response.ResultResponse;
import com.womenconcern.api.project.service.ResultService;
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
@SecurityRequirement(name = "bearer-jwt")
public class ResultController {

    private final ResultService resultService;

    @PostMapping
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    @Operation(summary = "Create a new result")
    public ResponseEntity<ResultResponse> createResult(
            @PathVariable UUID outcomeId,
            @Valid @RequestBody CreateResultRequest request) {
        ResultResponse response = resultService.createResult(outcomeId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{resultId}")
    @PreAuthorize("hasRole('PROJECT_MANAGER') or hasRole('FINANCE') or hasRole('EXECUTIVE_DIRECTOR')")
    @Operation(summary = "Get result by ID")
    public ResponseEntity<ResultResponse> getResult(
            @PathVariable UUID outcomeId,
            @PathVariable UUID resultId) {
        ResultResponse response = resultService.getResultById(resultId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('PROJECT_MANAGER') or hasRole('FINANCE') or hasRole('EXECUTIVE_DIRECTOR')")
    @Operation(summary = "Get all results for an outcome")
    public ResponseEntity<List<ResultResponse>> getResultsByOutcome(
            @PathVariable UUID outcomeId) {
        List<ResultResponse> response = resultService.getResultsByOutcome(outcomeId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{resultId}")
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    @Operation(summary = "Update result")
    public ResponseEntity<ResultResponse> updateResult(
            @PathVariable UUID outcomeId,
            @PathVariable UUID resultId,
            @Valid @RequestBody CreateResultRequest request) {
        ResultResponse response = resultService.updateResult(resultId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{resultId}")
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    @Operation(summary = "Delete result")
    public ResponseEntity<Void> deleteResult(
            @PathVariable UUID outcomeId,
            @PathVariable UUID resultId) {
        resultService.deleteResult(resultId);
        return ResponseEntity.noContent().build();
    }
}