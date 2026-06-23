package com.womenconcern.api.project.dto.response;

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
public class ActivityResponse {
    private UUID id;
    private String title;
    private String description;
    private BigDecimal costEstimate;
    private BigDecimal totalBudget;
    private UUID fieldOfficerId;
    private List<TaskResponse> tasks;
    private Instant createdAt;
    private Instant updatedAt;
}
