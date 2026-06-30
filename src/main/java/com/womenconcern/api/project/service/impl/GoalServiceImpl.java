package com.womenconcern.api.project.service.impl;


import com.womenconcern.api.exception.ResourceNotFoundException;
import com.womenconcern.api.project.dto.request.CreateGoalRequest;
import com.womenconcern.api.project.dto.request.UpdateGoalRequest;
import com.womenconcern.api.project.dto.response.GoalResponse;
import com.womenconcern.api.project.entity.Goal;
import com.womenconcern.api.project.entity.Project;
import com.womenconcern.api.project.repository.GoalRepository;
import com.womenconcern.api.project.repository.ProjectRepository;
import com.womenconcern.api.project.service.BudgetService;
import com.womenconcern.api.project.service.GoalService;
import com.womenconcern.api.utils.GoalMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoalServiceImpl implements GoalService {

    private final GoalRepository goalRepository;
    private final ProjectRepository projectRepository;
    private final GoalMapper goalMapper;
    private final BudgetService budgetService;

    @Override
    @Transactional
    public List<GoalResponse> createGoals(UUID projectId, CreateGoalRequest request) {

        log.info("Creating {} goals for project: {}",
                request.getGoals().size(), projectId);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Project not found with ID: " + projectId));

        List<Goal> goals = request.getGoals().stream()
                .map(g -> Goal.builder()
                        .project(project)
                        .title(g.getTitle())
                        .description(g.getDescription())
                        .build())
                .toList();

        List<Goal> saved = goalRepository.saveAll(goals);

        budgetService.recalculateBudgetForProject(projectId);

        return saved.stream()
                .map(goalMapper::toResponse)
                .toList();
    }
    @Override
    @Transactional(readOnly = true)
    public GoalResponse getGoalById(UUID goalId) {

        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Goal not found with ID: " + goalId));

        return goalMapper.toResponse(goal);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GoalResponse> getGoalsByProject(UUID projectId) {

        projectRepository.findById(projectId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Project not found with ID: " + projectId));

        List<Goal> goals = goalRepository.findByProjectId(projectId);

        return goals.stream()
                .map(goalMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public GoalResponse updateGoal(UUID goalId, UpdateGoalRequest request) {

        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Goal not found with ID: " + goalId));

        goal.setTitle(request.getTitle());
        goal.setDescription(request.getDescription());

        Goal updated = goalRepository.save(goal);

        budgetService.recalculateBudgetForProject(goal.getProject().getId());

        log.info("Goal updated: {}", goalId);

        return goalMapper.toResponse(updated);
    }
    @Override
    @Transactional
    public void deleteGoal(UUID goalId) {

        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Goal not found with ID: " + goalId));

        UUID projectId = goal.getProject().getId();

        goalRepository.delete(goal);

        budgetService.recalculateBudgetForProject(projectId);

        log.info("Goal deleted: {}", goalId);
    }


}