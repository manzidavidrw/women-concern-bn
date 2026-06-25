package com.womenconcern.api.project.dto.response;

import com.womenconcern.api.project.enums.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskResponse {
    private UUID id;
    private String title;
    private String description;
    private BigDecimal costEstimate;
    private TaskStatus status;
    private UUID fieldOfficerId;
    private ApprovalStatus projectManagerApprovalStatus;
    private ApprovalStatus financeApprovalStatus;
    private ApprovalStatus executiveApprovalStatus;
    private Instant createdAt;
    private Instant updatedAt;
}
