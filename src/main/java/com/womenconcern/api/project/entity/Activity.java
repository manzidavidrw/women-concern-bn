package com.womenconcern.api.project.entity;


import com.womenconcern.api.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "activities")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Activity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "result_id", nullable = false)
    private Result result;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "cost_estimate")
    private BigDecimal costEstimate;

    @Column(name = "total_budget")
    private BigDecimal totalBudget;

    @Column(name = "field_officer_id")
    private UUID fieldOfficerId; // Keycloak user UUID

    @OneToMany(mappedBy = "activity", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Fetch(FetchMode.SUBSELECT)
    private List<Task> tasks = new ArrayList<>();

    public BigDecimal calculateTotalBudget() {
        BigDecimal tasksBudget = tasks.stream()
                .map(Task::getCostEstimate)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return costEstimate != null ? costEstimate.add(tasksBudget) : tasksBudget;
    }

    public void updateTotalBudget() {
        this.totalBudget = calculateTotalBudget();
    }
}