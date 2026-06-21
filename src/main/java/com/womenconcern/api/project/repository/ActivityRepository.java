package com.womenconcern.api.project.repository;


import com.womenconcern.api.project.entity.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, UUID> {
    List<Activity> findByResultId(UUID resultId);

    List<Activity> findByFieldOfficerId(UUID fieldOfficerId);

    @Query("SELECT COALESCE(SUM(a.totalBudget), 0) FROM Activity a WHERE a.result.outcome.goal.project.id = :projectId")
    BigDecimal sumTotalBudgetByProjectId(@Param("projectId") UUID projectId);
    // ActivityRepository.java

    @Query("SELECT COALESCE(SUM(a.totalBudget), 0) FROM Activity a " +
            "WHERE a.result.outcome.goal.project.id = :projectId AND a.id != :excludeActivityId")
    BigDecimal sumTotalBudgetByProjectIdExcluding(
            @Param("projectId") UUID projectId,
            @Param("excludeActivityId") UUID excludeActivityId);
}
