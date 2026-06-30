package com.womenconcern.api.project.service;


import com.womenconcern.api.project.dto.request.CreateGoalRequest;
import com.womenconcern.api.project.dto.request.UpdateGoalRequest;
import com.womenconcern.api.project.dto.response.GoalResponse;

import java.util.List;
import java.util.UUID;

public interface GoalService {

    List<GoalResponse> createGoals(UUID projectId, CreateGoalRequest request);

    GoalResponse getGoalById(UUID goalId);

    List<GoalResponse> getGoalsByProject(UUID projectId);

    GoalResponse updateGoal(UUID goalId, UpdateGoalRequest request);

    void deleteGoal(UUID goalId);
}