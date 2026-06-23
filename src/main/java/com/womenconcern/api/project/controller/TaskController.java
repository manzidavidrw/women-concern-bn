// TaskController.java
package com.womenconcern.api.project.controller;

import com.womenconcern.api.project.dto.request.*;
import com.womenconcern.api.project.dto.response.TaskResponse;
import com.womenconcern.api.project.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/activities/{activityId}/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks", description = "Task management endpoints")
@SecurityRequirement(name = "bearer-jwt")
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    @PreAuthorize("hasRole('FIELD_OFFICER')")
    @Operation(summary = "Create a new task", description = "Only Field Officers can create tasks")
    public ResponseEntity<TaskResponse> createTask(
            @PathVariable UUID activityId,
            @Valid @RequestBody CreateTaskRequest request,
            Authentication authentication) {
        UUID fieldOfficerId = UUID.fromString(authentication.getName());
        TaskResponse response = taskService.createTask(activityId, request, fieldOfficerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{taskId}")
    @PreAuthorize("hasRole('FIELD_OFFICER') or hasRole('PROJECT_MANAGER') or hasRole('FINANCE') or hasRole('EXECUTIVE_DIRECTOR')")
    @Operation(summary = "Get task by ID")
    public ResponseEntity<TaskResponse> getTask(@PathVariable UUID taskId) {
        TaskResponse response = taskService.getTaskById(taskId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('FIELD_OFFICER') or hasRole('PROJECT_MANAGER') or hasRole('FINANCE') or hasRole('EXECUTIVE_DIRECTOR')")
    @Operation(summary = "Get all tasks for an activity")
    public ResponseEntity<List<TaskResponse>> getTasksByActivity(@PathVariable UUID activityId) {
        List<TaskResponse> response = taskService.getTasksByActivity(activityId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{taskId}")
    @PreAuthorize("hasRole('FIELD_OFFICER')")
    @Operation(summary = "Update task")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable UUID taskId,
            @Valid @RequestBody CreateTaskRequest request,
            Authentication authentication) {
        UUID fieldOfficerId = UUID.fromString(authentication.getName());
        TaskResponse response = taskService.updateTask(taskId, request, fieldOfficerId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{taskId}")
    @PreAuthorize("hasRole('FIELD_OFFICER')")
    @Operation(summary = "Delete task")
    public ResponseEntity<Void> deleteTask(
            @PathVariable UUID taskId,
            Authentication authentication) {
        UUID fieldOfficerId = UUID.fromString(authentication.getName());
        taskService.deleteTask(taskId, fieldOfficerId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{taskId}/submit-for-approval")
    @PreAuthorize("hasRole('FIELD_OFFICER')")
    @Operation(summary = "Submit task for approval")
    public ResponseEntity<TaskResponse> submitForApproval(
            @PathVariable UUID taskId,
            Authentication authentication) {
        UUID fieldOfficerId = UUID.fromString(authentication.getName());
        TaskResponse response = taskService.submitTaskForApproval(taskId, fieldOfficerId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{taskId}/approve-by-pm")
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    @Operation(summary = "Approve task by project manager")
    public ResponseEntity<TaskResponse> approveByProjectManager(
            @PathVariable UUID taskId,
            @Valid @RequestBody ApproveTaskRequest request,
            Authentication authentication) {
        UUID projectManagerId = UUID.fromString(authentication.getName());
        TaskResponse response = taskService.approveTaskByProjectManager(
                taskId, request.getApprovalNotes(), projectManagerId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{taskId}/reject-by-pm")
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    @Operation(summary = "Reject task by project manager")
    public ResponseEntity<TaskResponse> rejectByProjectManager(
            @PathVariable UUID taskId,
            @Valid @RequestBody RejectTaskRequest request,
            Authentication authentication) {
        UUID projectManagerId = UUID.fromString(authentication.getName());
        TaskResponse response = taskService.rejectTaskByProjectManager(
                taskId, request.getRejectionReason(), projectManagerId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{taskId}/approve-by-finance")
    @PreAuthorize("hasRole('FINANCE')")
    @Operation(summary = "Approve task by finance team")
    public ResponseEntity<TaskResponse> approveByFinance(
            @PathVariable UUID taskId,
            @Valid @RequestBody ApproveTaskRequest request,
            Authentication authentication) {
        UUID financeTeamMemberId = UUID.fromString(authentication.getName());
        TaskResponse response = taskService.approveTaskByFinance(
                taskId, request.getApprovalNotes(), financeTeamMemberId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{taskId}/reject-by-finance")
    @PreAuthorize("hasRole('FINANCE')")
    @Operation(summary = "Reject task by finance team")
    public ResponseEntity<TaskResponse> rejectByFinance(
            @PathVariable UUID taskId,
            @Valid @RequestBody RejectTaskRequest request,
            Authentication authentication) {
        UUID financeTeamMemberId = UUID.fromString(authentication.getName());
        TaskResponse response = taskService.rejectTaskByFinance(
                taskId, request.getRejectionReason(), financeTeamMemberId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{taskId}/approve-by-executive")
    @PreAuthorize("hasRole('EXECUTIVE_DIRECTOR')")
    @Operation(summary = "Approve task by executive director")
    public ResponseEntity<TaskResponse> approveByExecutive(
            @PathVariable UUID taskId,
            @Valid @RequestBody ApproveTaskRequest request,
            Authentication authentication) {
        UUID executiveDirectorId = UUID.fromString(authentication.getName());
        TaskResponse response = taskService.approveTaskByExecutive(
                taskId, request.getApprovalNotes(), executiveDirectorId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{taskId}/reject-by-executive")
    @PreAuthorize("hasRole('EXECUTIVE_DIRECTOR')")
    @Operation(summary = "Reject task by executive director")
    public ResponseEntity<TaskResponse> rejectByExecutive(
            @PathVariable UUID taskId,
            @Valid @RequestBody RejectTaskRequest request,
            Authentication authentication) {
        UUID executiveDirectorId = UUID.fromString(authentication.getName());
        TaskResponse response = taskService.rejectTaskByExecutive(
                taskId, request.getRejectionReason(), executiveDirectorId);
        return ResponseEntity.ok(response);
    }
}