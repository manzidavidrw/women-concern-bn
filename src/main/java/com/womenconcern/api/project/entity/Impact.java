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
@Table(name = "impacts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Impact extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "objective_id", nullable = false)
    private Objective objective;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "total_budget")
    private BigDecimal totalBudget;

    @OneToMany(mappedBy = "impact", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Fetch(FetchMode.SUBSELECT)
    private List<Outcome> outcomes = new ArrayList<>();

    public BigDecimal calculateTotalBudget() {
        return outcomes.stream()
                .map(Outcome::calculateTotalBudget)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void updateTotalBudget() {
        this.totalBudget = calculateTotalBudget();
    }
}