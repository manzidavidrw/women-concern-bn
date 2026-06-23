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
@Table(name = "outcomes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Outcome extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "impact_id", nullable = false)
    private Impact impact;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "total_budget")
    private BigDecimal totalBudget;

    @OneToMany(mappedBy = "outcome", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Fetch(FetchMode.SUBSELECT)
    private List<Result> results = new ArrayList<>();

    public BigDecimal calculateTotalBudget() {
        return results.stream()
                .map(Result::calculateTotalBudget)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void updateTotalBudget() {
        this.totalBudget = calculateTotalBudget();
    }
}