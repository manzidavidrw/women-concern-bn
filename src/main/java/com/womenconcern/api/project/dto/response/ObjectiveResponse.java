package com.womenconcern.api.project.dto.response;

// ObjectiveResponse.java

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ObjectiveResponse {
    private UUID id;
    private String title;
    private String description;
    private BigDecimal totalBudget;
    private List<ImpactResponse> impacts;
    private Instant createdAt;
    private Instant updatedAt;
}