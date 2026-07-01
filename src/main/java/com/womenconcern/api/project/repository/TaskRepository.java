package com.womenconcern.api.project.repository;



import com.womenconcern.api.project.entity.Task;
import com.womenconcern.api.project.enums.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;


import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {
    List<Task> findByActivityId(UUID activityId);

    List<Task> findByFieldOfficerId(UUID fieldOfficerId);

    List<Task> findByStatus(TaskStatus status);

    List<Task> findByProjectManagerApprovalStatus(ApprovalStatus status);

    List<Task> findByFinanceApprovalStatus(ApprovalStatus status);

    @Query("SELECT t FROM Task t WHERE t.activity.result.outcome.goal.project.id = ?1")
    List<Task> findTasksByProjectId(UUID projectId);

    @Query("SELECT COALESCE(SUM(t.costEstimate), 0) FROM Task t WHERE t.activity.id = :activityId")
    BigDecimal sumCostEstimateByActivityId(@Param("activityId") UUID activityId);
}