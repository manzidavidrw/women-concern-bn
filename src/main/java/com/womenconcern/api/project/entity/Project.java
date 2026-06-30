package com.womenconcern.api.project.entity;

import com.womenconcern.api.project.enums.ApprovalStatus;
import com.womenconcern.api.project.enums.ProjectStatus;
import com.womenconcern.api.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "projects")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "project_manager_id", nullable = false)
    private UUID projectManagerId;

    @Column(name = "approved_budget")
    private BigDecimal approvedBudget;

    @Column(name = "total_budget")
    private BigDecimal totalBudget;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectStatus status;

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

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Fetch(FetchMode.SUBSELECT)
    @Builder.Default
    private List<Goal> goals = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (status == null) status = ProjectStatus.DRAFT;
        if (projectManagerApprovalStatus == null) projectManagerApprovalStatus = ApprovalStatus.PENDING;
        if (financeApprovalStatus == null) financeApprovalStatus = ApprovalStatus.PENDING;
        if (executiveApprovalStatus == null) executiveApprovalStatus = ApprovalStatus.PENDING;
    }

    public BigDecimal calculateTotalBudget() {
        return goals.stream()
                .map(Goal::calculateTotalBudget)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}