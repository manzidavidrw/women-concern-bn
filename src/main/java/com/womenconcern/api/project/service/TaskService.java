package com.womenconcern.api.project.service;

// TaskService.java


import com.womenconcern.api.project.dto.request.CreateTaskRequest;
import com.womenconcern.api.project.dto.response.TaskResponse;
import java.util.List;
import java.util.UUID;

public interface TaskService {
    TaskResponse createTask(UUID activityId, CreateTaskRequest request, UUID fieldOfficerId);

    TaskResponse getTaskById(UUID taskId);

    List<TaskResponse> getTasksByActivity(UUID activityId);

    List<TaskResponse> getTasksByFieldOfficer(UUID fieldOfficerId);

    TaskResponse updateTask(UUID taskId, CreateTaskRequest request, UUID fieldOfficerId);

    void deleteTask(UUID taskId, UUID fieldOfficerId);

    TaskResponse submitTaskForApproval(UUID taskId, UUID fieldOfficerId);

    TaskResponse approveTaskByProjectManager(UUID taskId, String approvalNotes, UUID projectManagerId);

    TaskResponse rejectTaskByProjectManager(UUID taskId, String rejectionReason, UUID projectManagerId);

    TaskResponse approveTaskByFinance(UUID taskId, String approvalNotes, UUID financeTeamMemberId);

    TaskResponse rejectTaskByFinance(UUID taskId, String rejectionReason, UUID financeTeamMemberId);

    TaskResponse approveTaskByExecutive(UUID taskId, String approvalNotes, UUID executiveDirectorId);

    TaskResponse rejectTaskByExecutive(UUID taskId, String rejectionReason, UUID executiveDirectorId);
}