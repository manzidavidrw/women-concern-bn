package com.womenconcern.api.project.service;


import com.womenconcern.api.project.entity.Activity;
import com.womenconcern.api.project.entity.Project;
import java.util.UUID;

public interface BudgetService {
    void recalculateBudgetForProject(UUID projectId);

    void recalculateBudgetForActivity(Activity activity);
}
