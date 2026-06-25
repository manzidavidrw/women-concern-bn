package com.womenconcern.api.project.service.impl;


import com.womenconcern.api.exception.ResourceNotFoundException;
import com.womenconcern.api.project.entity.*;
import com.womenconcern.api.project.repository.*;
import com.womenconcern.api.project.service.BudgetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BudgetServiceImpl implements BudgetService {

    private final ProjectRepository projectRepository;
    private final ObjectiveRepository objectiveRepository;
    private final ImpactRepository impactRepository;
    private final OutcomeRepository outcomeRepository;
    private final ResultRepository resultRepository;
    private final ActivityRepository activityRepository;

    @Override
    @Transactional
    public void recalculateBudgetForProject(UUID projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + projectId));

        project.setTotalBudget(project.calculateTotalBudget());
        projectRepository.save(project);

        log.info("Budget recalculated for project: {}", projectId);
    }

    @Override
    @Transactional
    public void recalculateBudgetForActivity(Activity activity) {
        // Update activity budget
        activity.updateTotalBudget();
        activityRepository.save(activity);

        // Update result budget
        Result result = activity.getResult();
        result.updateTotalBudget();
        resultRepository.save(result);

        // Update outcome budget
        Outcome outcome = result.getOutcome();
        outcome.updateTotalBudget();
        outcomeRepository.save(outcome);

        // Update impact budget
        Impact impact = outcome.getImpact();
        impact.updateTotalBudget();
        impactRepository.save(impact);

        // Update objective budget
        Objective objective = impact.getObjective();
        objective.updateTotalBudget();
        objectiveRepository.save(objective);

        // Update project budget
        Project project = objective.getProject();
        project.setTotalBudget(project.calculateTotalBudget());
        projectRepository.save(project);

        log.info("Budget recalculated up the hierarchy from activity: {}", activity.getId());
    }
}