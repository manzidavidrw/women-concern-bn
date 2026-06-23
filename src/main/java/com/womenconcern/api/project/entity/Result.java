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
@Table(name = "results")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Result extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "outcome_id", nullable = false)
    private Outcome outcome;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "total_budget")
    private BigDecimal totalBudget;

    @OneToMany(mappedBy = "result", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Fetch(FetchMode.SUBSELECT)
    private List<Activity> activities = new ArrayList<>();

    public BigDecimal calculateTotalBudget() {
        return activities.stream()
                .map(Activity::calculateTotalBudget)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void updateTotalBudget() {
        this.totalBudget = calculateTotalBudget();
    }
}
