package com.womenconcern.api.project.entity;


import com.womenconcern.api.project.enums.ApprovalStatus;
import com.womenconcern.api.project.enums.TaskStatus;
import com.womenconcern.api.shared.BaseEntity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", nullable = false)
    private Activity activity;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "cost_estimate", nullable = false)
    private BigDecimal costEstimate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status;

    @Column(name = "field_officer_id", nullable = false)
    private UUID fieldOfficerId; // Keycloak user UUID who created the task

    // Approvals
    @Enumerated(EnumType.STRING)
    @Column(name = "project_manager_approval_status")
    private ApprovalStatus projectManagerApprovalStatus;

    @Column(name = "project_manager_approval_notes", columnDefinition = "TEXT")
    private String projectManagerApprovalNotes;

    @Column(name = "project_manager_approved_by")
    private UUID projectManagerApprovedBy;

    @Column(name = "project_manager_approved_at")
    private Instant projectManagerApprovedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "finance_approval_status")
    private ApprovalStatus financeApprovalStatus;

    @Column(name = "finance_approval_notes", columnDefinition = "TEXT")
    private String financeApprovalNotes;

    @Column(name = "finance_approved_by")
    private UUID financeApprovedBy;

    @Column(name = "finance_approved_at")
    private Instant financeApprovedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "executive_approval_status")
    private ApprovalStatus executiveApprovalStatus;

    @Column(name = "executive_approval_notes", columnDefinition = "TEXT")
    private String executiveApprovalNotes;

    @Column(name = "executive_approved_by")
    private UUID executiveApprovedBy;

    @Column(name = "executive_approved_at")
    private Instant executiveApprovedAt;

    @PrePersist
    protected void onCreate() {
        if (status == null) {
            status = TaskStatus.DRAFT;
        }
        if (projectManagerApprovalStatus == null) {
            projectManagerApprovalStatus = ApprovalStatus.PENDING;
        }
        if (financeApprovalStatus == null) {
            financeApprovalStatus = ApprovalStatus.PENDING;
        }
        if (executiveApprovalStatus == null) {
            executiveApprovalStatus = ApprovalStatus.PENDING;
        }
    }

    public boolean isFullyApproved() {
        return projectManagerApprovalStatus == ApprovalStatus.APPROVED &&
                financeApprovalStatus == ApprovalStatus.APPROVED &&
                executiveApprovalStatus == ApprovalStatus.APPROVED;
    }
}