package com.womenconcern.api.project.service;

import com.womenconcern.api.project.entity.Activity;
import com.womenconcern.api.project.entity.Task;

import java.util.List;
import java.util.UUID;

public interface BudgetService {
    void recalculateBudgetForTask(Task task);
    void recalculateBudgetForActivity(Activity activity);
    void recalculateBudgetForProject(UUID projectId);
    void validateActivityBudget(Activity activity);   // ← add
    void validateTaskBudget(Task task);
    void validateActivityBatch(List<Activity> activities);  // ← ADD
    void validateTaskBatch(List<Task> tasks);               // ← add
}