package com.womenconcern.api.project.service.impl;



import com.womenconcern.api.exception.ResourceNotFoundException;
import com.womenconcern.api.exception.UnauthorizedException;
import com.womenconcern.api.project.dto.request.CreateTaskRequest;
import com.womenconcern.api.project.dto.response.TaskResponse;
import com.womenconcern.api.project.entity.Activity;
import com.womenconcern.api.project.entity.Task;
import com.womenconcern.api.project.enums.ApprovalStatus;
import com.womenconcern.api.project.enums.TaskStatus;
import com.womenconcern.api.project.repository.ActivityRepository;
import com.womenconcern.api.project.repository.TaskRepository;
import com.womenconcern.api.project.service.BudgetService;
import com.womenconcern.api.project.service.TaskService;
import com.womenconcern.api.utils.TaskMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final ActivityRepository activityRepository;
    private final TaskMapper taskMapper;
    private final BudgetService budgetService;

    @Override
    @Transactional
    public TaskResponse createTask(UUID activityId, CreateTaskRequest request, UUID fieldOfficerId) {
        log.info("Creating task for activity: {}", activityId);

        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found with ID: " + activityId));

        Task task = Task.builder()
                .activity(activity)
                .title(request.getTitle())
                .description(request.getDescription())
                .costEstimate(request.getCostEstimate())
                .fieldOfficerId(fieldOfficerId)
                .status(TaskStatus.DRAFT)
                .projectManagerApprovalStatus(ApprovalStatus.PENDING)
                .financeApprovalStatus(ApprovalStatus.PENDING)
                .executiveApprovalStatus(ApprovalStatus.PENDING)
                .build();

        Task savedTask = taskRepository.save(task);

        // Update activity budget
        activity.updateTotalBudget();
        activityRepository.save(activity);

        // Trigger budget recalculation up the hierarchy
        budgetService.recalculateBudgetForActivity(activity);

        log.info("Task created successfully with ID: {}", savedTask.getId());
        return taskMapper.toResponse(savedTask);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponse getTaskById(UUID taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + taskId));

        return taskMapper.toResponse(task);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByActivity(UUID activityId) {
        List<Task> tasks = taskRepository.findByActivityId(activityId);
        return tasks.stream()
                .map(taskMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> getTasksByFieldOfficer(UUID fieldOfficerId) {
        List<Task> tasks = taskRepository.findByFieldOfficerId(fieldOfficerId);
        return tasks.stream()
                .map(taskMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public TaskResponse updateTask(UUID taskId, CreateTaskRequest request, UUID fieldOfficerId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + taskId));

        if (!task.getFieldOfficerId().equals(fieldOfficerId)) {
            throw new UnauthorizedException("Only the task creator can update this task");
        }

        if (task.getStatus() != TaskStatus.DRAFT && task.getStatus() != TaskStatus.REJECTED) {
            throw new IllegalStateException("Task can only be updated in DRAFT or REJECTED status");
        }

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setCostEstimate(request.getCostEstimate());

        Task updatedTask = taskRepository.save(task);

        // Update activity budget
        Activity activity = task.getActivity();
        activity.updateTotalBudget();
        activityRepository.save(activity);

        // Trigger budget recalculation up the hierarchy
        budgetService.recalculateBudgetForActivity(activity);

        return taskMapper.toResponse(updatedTask);
    }

    @Override
    @Transactional
    public void deleteTask(UUID taskId, UUID fieldOfficerId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + taskId));

        if (!task.getFieldOfficerId().equals(fieldOfficerId)) {
            throw new UnauthorizedException("Only the task creator can delete this task");
        }

        if (task.getStatus() != TaskStatus.DRAFT) {
            throw new IllegalStateException("Task can only be deleted in DRAFT status");
        }

        Activity activity = task.getActivity();
        taskRepository.delete(task);

        // Update activity budget
        activity.updateTotalBudget();
        activityRepository.save(activity);

        // Trigger budget recalculation up the hierarchy
        budgetService.recalculateBudgetForActivity(activity);

        log.info("Task deleted: {}", taskId);
    }

    @Override
    @Transactional
    public TaskResponse submitTaskForApproval(UUID taskId, UUID fieldOfficerId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + taskId));

        if (!task.getFieldOfficerId().equals(fieldOfficerId)) {
            throw new UnauthorizedException("Only the task creator can submit this task");
        }

        if (task.getStatus() != TaskStatus.DRAFT && task.getStatus() != TaskStatus.REJECTED) {
            throw new IllegalStateException("Task must be in DRAFT or REJECTED status to submit");
        }

        task.setStatus(TaskStatus.SUBMITTED_FOR_APPROVAL);
        Task updatedTask = taskRepository.save(task);

        log.info("Task submitted for approval: {}", taskId);
        return taskMapper.toResponse(updatedTask);
    }

    @Override
    @Transactional
    public TaskResponse approveTaskByProjectManager(UUID taskId, String approvalNotes, UUID projectManagerId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + taskId));

        if (task.getStatus() != TaskStatus.SUBMITTED_FOR_APPROVAL) {
            throw new IllegalStateException("Task must be in SUBMITTED_FOR_APPROVAL status");
        }

        task.setProjectManagerApprovalStatus(ApprovalStatus.APPROVED);
        task.setProjectManagerApprovalNotes(approvalNotes);
        task.setProjectManagerApprovedBy(projectManagerId);
        task.setProjectManagerApprovedAt(Instant.now());

        Task updatedTask = taskRepository.save(task);
        log.info("Task approved by project manager: {}", taskId);

        return taskMapper.toResponse(updatedTask);
    }

    @Override
    @Transactional
    public TaskResponse rejectTaskByProjectManager(UUID taskId, String rejectionReason, UUID projectManagerId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + taskId));

        if (task.getStatus() != TaskStatus.SUBMITTED_FOR_APPROVAL) {
            throw new IllegalStateException("Task must be in SUBMITTED_FOR_APPROVAL status");
        }

        task.setStatus(TaskStatus.REJECTED);
        task.setProjectManagerApprovalStatus(ApprovalStatus.REJECTED);
        task.setProjectManagerApprovalNotes(rejectionReason);
        task.setProjectManagerApprovedBy(projectManagerId);
        task.setProjectManagerApprovedAt(Instant.now());

        Task updatedTask = taskRepository.save(task);
        log.info("Task rejected by project manager: {}", taskId);

        return taskMapper.toResponse(updatedTask);
    }

    @Override
    @Transactional
    public TaskResponse approveTaskByFinance(UUID taskId, String approvalNotes, UUID financeTeamMemberId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + taskId));

        if (task.getProjectManagerApprovalStatus() != ApprovalStatus.APPROVED) {
            throw new IllegalStateException("Project manager must approve the task first");
        }

        task.setFinanceApprovalStatus(ApprovalStatus.APPROVED);
        task.setFinanceApprovalNotes(approvalNotes);
        task.setFinanceApprovedBy(financeTeamMemberId);
        task.setFinanceApprovedAt(Instant.now());

        Task updatedTask = taskRepository.save(task);
        log.info("Task approved by finance team: {}", taskId);

        return taskMapper.toResponse(updatedTask);
    }

    @Override
    @Transactional
    public TaskResponse rejectTaskByFinance(UUID taskId, String rejectionReason, UUID financeTeamMemberId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + taskId));

        if (task.getProjectManagerApprovalStatus() != ApprovalStatus.APPROVED) {
            throw new IllegalStateException("Project manager must approve the task first");
        }

        task.setStatus(TaskStatus.REJECTED);
        task.setFinanceApprovalStatus(ApprovalStatus.REJECTED);
        task.setFinanceApprovalNotes(rejectionReason);
        task.setFinanceApprovedBy(financeTeamMemberId);
        task.setFinanceApprovedAt(Instant.now());

        Task updatedTask = taskRepository.save(task);
        log.info("Task rejected by finance team: {}", taskId);

        return taskMapper.toResponse(updatedTask);
    }

    @Override
    @Transactional
    public TaskResponse approveTaskByExecutive(UUID taskId, String approvalNotes, UUID executiveDirectorId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + taskId));

        if (task.getFinanceApprovalStatus() != ApprovalStatus.APPROVED) {
            throw new IllegalStateException("Finance team must approve the task first");
        }

        task.setStatus(TaskStatus.APPROVED);
        task.setExecutiveApprovalStatus(ApprovalStatus.APPROVED);
        task.setExecutiveApprovalNotes(approvalNotes);
        task.setExecutiveApprovedBy(executiveDirectorId);
        task.setExecutiveApprovedAt(Instant.now());

        Task updatedTask = taskRepository.save(task);
        log.info("Task approved by executive director: {}", taskId);

        return taskMapper.toResponse(updatedTask);
    }

    @Override
    @Transactional
    public TaskResponse rejectTaskByExecutive(UUID taskId, String rejectionReason, UUID executiveDirectorId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with ID: " + taskId));

        if (task.getFinanceApprovalStatus() != ApprovalStatus.APPROVED) {
            throw new IllegalStateException("Finance team must approve the task first");
        }

        task.setStatus(TaskStatus.REJECTED);
        task.setExecutiveApprovalStatus(ApprovalStatus.REJECTED);
        task.setExecutiveApprovalNotes(rejectionReason);
        task.setExecutiveApprovedBy(executiveDirectorId);
        task.setExecutiveApprovedAt(Instant.now());

        Task updatedTask = taskRepository.save(task);
        log.info("Task rejected by executive director: {}", taskId);

        return taskMapper.toResponse(updatedTask);
    }
}
