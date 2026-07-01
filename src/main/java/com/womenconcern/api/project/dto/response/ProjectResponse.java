package com.womenconcern.api.project.dto.response;



import com.womenconcern.api.project.enums.*;
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
public class ProjectResponse {
    private UUID id;
    private String name;
    private String description;
    private UUID projectManagerId;
    private BigDecimal totalBudget;
    private ProjectStatus status;
    private ApprovalStatus projectManagerApprovalStatus;
    private ApprovalStatus financeApprovalStatus;
    private ApprovalStatus executiveApprovalStatus;
    private List<GoalResponse> objectives;
    private Instant createdAt;
    private Instant updatedAt;
}