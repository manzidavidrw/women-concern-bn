package com.womenconcern.api.project.service.impl;

import com.womenconcern.api.exception.ResourceNotFoundException;
import com.womenconcern.api.project.entity.*;
import com.womenconcern.api.project.repository.*;
import com.womenconcern.api.project.service.BudgetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BudgetServiceImpl implements BudgetService {

    private final ProjectRepository projectRepository;
    private final ActivityRepository activityRepository;
    private final TaskRepository taskRepository;

    @Override
    @Transactional
    public void recalculateBudgetForTask(Task task) {
        recalculateFromActivity(task.getActivity());
    }

    @Override
    @Transactional
    public void recalculateBudgetForActivity(Activity activity) {
        recalculateFromActivity(activity);
    }

    @Override
    @Transactional
    public void recalculateBudgetForProject(UUID projectId) {
        recalculateProject(projectId);
    }

    // ── Validation ────────────────────────────────────────────────

    /**
     * Call this BEFORE saving a new or updated activity.
     * Checks that adding this activity's costEstimate won't push
     * the project over its totalBudget.
     */
    @Override
    public void validateActivityBudget(Activity activity) {
        UUID projectId = activity.getResult().getOutcome().getGoal().getProject().getId();
        Project project = getProject(projectId);

        if (project.getTotalBudget() == null) return; // no cap set

        // Sum of all OTHER activities already in this project
        BigDecimal otherActivitiesTotal = activityRepository
                .sumTotalBudgetByProjectIdExcluding(projectId, activity.getId() != null
                        ? activity.getId()
                        : UUID.fromString("00000000-0000-0000-0000-000000000000"));

        BigDecimal thisCost = activity.getCostEstimate() != null
                ? activity.getCostEstimate()
                : BigDecimal.ZERO;

        BigDecimal projectedTotal = otherActivitiesTotal.add(thisCost);

        if (projectedTotal.compareTo(project.getTotalBudget()) > 0) {
            BigDecimal remaining = project.getTotalBudget().subtract(otherActivitiesTotal);
            throw new IllegalArgumentException(String.format(
                    "Activity cost estimate %.2f exceeds remaining project budget %.2f " +
                            "(project budget: %.2f, already allocated: %.2f)",
                    thisCost, remaining, project.getTotalBudget(), otherActivitiesTotal
            ));
        }

        log.info("Activity budget validated — cost: {}, remaining project budget: {}",
                thisCost, project.getTotalBudget().subtract(otherActivitiesTotal));
    }

    /**
     * Call this BEFORE saving a new or updated task.
     * Checks that adding this task's costEstimate won't push
     * the activity (and by extension the project) over budget.
     */
    @Override
    public void validateTaskBudget(Task task) {
        Activity activity = task.getActivity();
        UUID projectId = activity.getResult().getOutcome().getGoal().getProject().getId();
        Project project = getProject(projectId);

        if (project.getTotalBudget() == null) return; // no cap set

        // Current project total excluding this activity
        BigDecimal otherActivitiesTotal = activityRepository
                .sumTotalBudgetByProjectIdExcluding(projectId, activity.getId());

        // Current task sum for this activity excluding the task being added/updated
        BigDecimal existingTaskSum = taskRepository.sumCostEstimateByActivityId(activity.getId());

        // Subtract old task cost if this is an update (task already has an ID)
        BigDecimal oldTaskCost = (task.getId() != null && task.getCostEstimate() != null)
                ? task.getCostEstimate()
                : BigDecimal.ZERO;

        BigDecimal newTaskCost = task.getCostEstimate() != null
                ? task.getCostEstimate()
                : BigDecimal.ZERO;

        BigDecimal activityOwnCost = activity.getCostEstimate() != null
                ? activity.getCostEstimate()
                : BigDecimal.ZERO;

        // Projected activity total after this task
        BigDecimal projectedActivityTotal = activityOwnCost
                .add(existingTaskSum)
                .subtract(oldTaskCost)
                .add(newTaskCost);

        // Projected project total
        BigDecimal projectedProjectTotal = otherActivitiesTotal.add(projectedActivityTotal);

        if (projectedProjectTotal.compareTo(project.getTotalBudget()) > 0) {
            BigDecimal projectRemaining = project.getTotalBudget().subtract(otherActivitiesTotal)
                    .subtract(activityOwnCost)
                    .subtract(existingTaskSum.subtract(oldTaskCost));
            throw new IllegalArgumentException(String.format(
                    "Task cost estimate %.2f exceeds remaining budget %.2f " +
                            "(project budget: %.2f, projected project total: %.2f)",
                    newTaskCost, projectRemaining,
                    project.getTotalBudget(), projectedProjectTotal
            ));
        }

        log.info("Task budget validated — task cost: {}, projected project total: {} / {}",
                newTaskCost, projectedProjectTotal, project.getTotalBudget());
    }



    private void recalculateFromActivity(Activity activity) {
        BigDecimal taskSum = taskRepository.sumCostEstimateByActivityId(activity.getId());
        BigDecimal ownCost = activity.getCostEstimate() != null ? activity.getCostEstimate() : BigDecimal.ZERO;
        BigDecimal activityTotal = ownCost.add(taskSum);

        activity.setTotalBudget(activityTotal);
        activityRepository.save(activity);

        UUID projectId = activity.getResult().getOutcome().getGoal().getProject().getId();
        recalculateProject(projectId);

        log.info("Budget recalculated — activity: {} totalBudget: {}", activity.getId(), activityTotal);
    }
    
    private Project getProject(UUID projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found: " + projectId));
    }

    @Override
    public void validateActivityBatch(List<Activity> activities) {
        if (activities == null || activities.isEmpty()) return;

        UUID projectId = activities.get(0).getResult().getOutcome().getGoal().getProject().getId();
        Project project = getProject(projectId);

        if (project.getApprovedBudget() == null) return; // no cap set

        BigDecimal alreadyAllocated = activityRepository.sumTotalBudgetByProjectId(projectId);

        BigDecimal batchTotal = activities.stream()
                .map(a -> a.getCostEstimate() != null ? a.getCostEstimate() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal projectedTotal = alreadyAllocated.add(batchTotal);

        if (projectedTotal.compareTo(project.getApprovedBudget()) > 0) {  // ← approvedBudget
            BigDecimal remaining = project.getApprovedBudget().subtract(alreadyAllocated);
            throw new IllegalArgumentException(String.format(
                    "Total activities cost %.2f exceeds remaining project budget %.2f " +
                            "(approved budget: %.2f, already allocated: %.2f)",
                    batchTotal, remaining, project.getApprovedBudget(), alreadyAllocated
            ));
        }
    }

    @Override
    public void validateTaskBatch(List<Task> tasks) {
        if (tasks == null || tasks.isEmpty()) return;

        Activity activity = tasks.get(0).getActivity();
        UUID projectId = activity.getResult().getOutcome().getGoal().getProject().getId();
        Project project = getProject(projectId);

        if (project.getApprovedBudget() == null) return;  // ← approvedBudget

        BigDecimal otherActivitiesTotal = activityRepository
                .sumTotalBudgetByProjectIdExcluding(projectId, activity.getId());
        BigDecimal existingTaskSum = taskRepository.sumCostEstimateByActivityId(activity.getId());
        BigDecimal activityOwnCost = activity.getCostEstimate() != null
                ? activity.getCostEstimate() : BigDecimal.ZERO;

        BigDecimal batchTotal = tasks.stream()
                .map(t -> t.getCostEstimate() != null ? t.getCostEstimate() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal projectedProjectTotal = otherActivitiesTotal
                .add(activityOwnCost)
                .add(existingTaskSum)
                .add(batchTotal);

        if (projectedProjectTotal.compareTo(project.getApprovedBudget()) > 0) {  // ← approvedBudget
            BigDecimal remaining = project.getApprovedBudget()
                    .subtract(otherActivitiesTotal)
                    .subtract(activityOwnCost)
                    .subtract(existingTaskSum);
            throw new IllegalArgumentException(String.format(
                    "Total tasks cost %.2f exceeds remaining budget %.2f " +
                            "(approved budget: %.2f, projected total: %.2f)",
                    batchTotal, remaining, project.getApprovedBudget(), projectedProjectTotal
            ));
        }
    }

    private void recalculateProject(UUID projectId) {
        Project project = getProject(projectId);
        BigDecimal projectTotal = activityRepository.sumTotalBudgetByProjectId(projectId);
        project.setTotalBudget(projectTotal);  // ← only touches totalBudget, never approvedBudget
        projectRepository.save(project);
        log.info("Project totalBudget recalculated: {} = {} (cap: {})",
                projectId, projectTotal, project.getApprovedBudget());
    }
}