package com.womenconcern.api.project.controller;


import com.womenconcern.api.project.dto.request.CreateOutcomeRequest;
import com.womenconcern.api.project.dto.response.OutcomeResponse;
import com.womenconcern.api.project.service.OutcomeService;
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
@RequestMapping("/api/v1/impacts/{impactId}/outcomes")
@RequiredArgsConstructor
@Tag(name = "Outcomes", description = "Outcome management endpoints")
@SecurityRequirement(name = "bearer-jwt")
public class OutcomeController {

    private final OutcomeService outcomeService;

    @PostMapping
    @PreAuthorize("hasRole('project-manager')")
    @Operation(summary = "Create a new outcome")
    public ResponseEntity<OutcomeResponse> createOutcome(
            @PathVariable UUID impactId,
            @Valid @RequestBody CreateOutcomeRequest request) {
        OutcomeResponse response = outcomeService.createOutcome(impactId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{outcomeId}")
    @PreAuthorize("hasRole('PROJECT_MANAGER') or hasRole('FINANCE') or hasRole('EXECUTIVE_DIRECTOR')")
    @Operation(summary = "Get outcome by ID")
    public ResponseEntity<OutcomeResponse> getOutcome(
            @PathVariable UUID impactId,
            @PathVariable UUID outcomeId) {
        OutcomeResponse response = outcomeService.getOutcomeById(outcomeId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('PROJECT_MANAGER') or hasRole('FINANCE') or hasRole('EXECUTIVE_DIRECTOR')")
    @Operation(summary = "Get all outcomes for an impact")
    public ResponseEntity<List<OutcomeResponse>> getOutcomesByImpact(
            @PathVariable UUID impactId) {
        List<OutcomeResponse> response = outcomeService.getOutcomesByImpact(impactId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{outcomeId}")
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    @Operation(summary = "Update outcome")
    public ResponseEntity<OutcomeResponse> updateOutcome(
            @PathVariable UUID impactId,
            @PathVariable UUID outcomeId,
            @Valid @RequestBody CreateOutcomeRequest request) {
        OutcomeResponse response = outcomeService.updateOutcome(outcomeId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{outcomeId}")
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    @Operation(summary = "Delete outcome")
    public ResponseEntity<Void> deleteOutcome(
            @PathVariable UUID impactId,
            @PathVariable UUID outcomeId) {
        outcomeService.deleteOutcome(outcomeId);
        return ResponseEntity.noContent().build();
    }
}