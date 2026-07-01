package com.womenconcern.api.project.repository;


import com.womenconcern.api.project.entity.Result;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface ResultRepository extends JpaRepository<Result, UUID> {
    List<Result> findByOutcomeId(UUID outcomeId);

    @Query("SELECT COALESCE(SUM(r.totalBudget), 0) FROM Result r WHERE r.outcome.id = :outcomeId")
    BigDecimal sumTotalBudgetByOutcomeId(@Param("outcomeId") UUID outcomeId);

}
