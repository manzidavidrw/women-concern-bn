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

@Entity
@Table(name = "objectives")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Objective extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "total_budget")
    private BigDecimal totalBudget;

    @OneToMany(mappedBy = "objective", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Fetch(FetchMode.SUBSELECT)
    private List<Impact> impacts = new ArrayList<>();

    public BigDecimal calculateTotalBudget() {
        return impacts.stream()
                .map(Impact::calculateTotalBudget)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void updateTotalBudget() {
        this.totalBudget = calculateTotalBudget();
    }
}
