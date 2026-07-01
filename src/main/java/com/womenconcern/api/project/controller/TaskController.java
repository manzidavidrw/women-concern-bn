package com.womenconcern.api.project.controller;

import com.womenconcern.api.auth.entity.User;
import com.womenconcern.api.project.dto.request.*;
import com.womenconcern.api.project.dto.response.TaskResponse;
import com.womenconcern.api.project.service.TaskService;
import com.womenconcern.api.utils.ApiResponse;
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
@SecurityRequirement(name = "bearerAuth")
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    @PreAuthorize("hasRole('FIELD_OFFICER')")
    @Operation(summary = "Create tasks for an activity")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> createTasks(
            @PathVariable UUID activityId,
            @Valid @RequestBody CreateTaskRequest request,
            Authentication auth) {

        UUID fieldOfficerId = ((User) auth.getPrincipal()).getId();
        List<TaskResponse> tasks = taskService.createTasks(activityId, request, fieldOfficerId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Tasks created successfully", tasks));
    }

    @GetMapping("/{taskId}")
    @PreAuthorize("hasRole('FIELD_OFFICER') or hasRole('PROJECT_MANAGER') or hasRole('FINANCE') or hasRole('EXECUTIVE_DIRECTOR')")
    @Operation(summary = "Get task by ID")
    public ResponseEntity<ApiResponse<TaskResponse>> getTask(@PathVariable UUID taskId) {

        return ResponseEntity.ok(new ApiResponse<>(true, "Task retrieved successfully", taskService.getTaskById(taskId)));
    }

    @GetMapping
    @PreAuthorize("hasRole('FIELD_OFFICER') or hasRole('PROJECT_MANAGER') or hasRole('FINANCE') or hasRole('EXECUTIVE_DIRECTOR')")
    @Operation(summary = "Get all tasks for an activity")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getTasksByActivity(@PathVariable UUID activityId) {

        return ResponseEntity.ok(new ApiResponse<>(true, "Tasks retrieved successfully", taskService.getTasksByActivity(activityId)));
    }

    @PutMapping("/{taskId}")
    @PreAuthorize("hasRole('FIELD_OFFICER')")
    @Operation(summary = "Update a single task")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTask(
            @PathVariable UUID activityId,
            @PathVariable UUID taskId,
            @Valid @RequestBody UpdateTaskRequest request,
            Authentication auth) {

        UUID fieldOfficerId = ((User) auth.getPrincipal()).getId();
        return ResponseEntity.ok(new ApiResponse<>(true, "Task updated successfully",
                taskService.updateTask(taskId, request, fieldOfficerId)));
    }

    @DeleteMapping("/{taskId}")
    @PreAuthorize("hasRole('FIELD_OFFICER')")
    @Operation(summary = "Delete task")
    public ResponseEntity<ApiResponse<Void>> deleteTask(
            @PathVariable UUID taskId,
            Authentication auth) {

        UUID fieldOfficerId = ((User) auth.getPrincipal()).getId();
        taskService.deleteTask(taskId, fieldOfficerId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Task deleted successfully", null));
    }

    @PostMapping("/{taskId}/submit-for-approval")
    @PreAuthorize("hasRole('FIELD_OFFICER')")
    @Operation(summary = "Submit task for approval")
    public ResponseEntity<ApiResponse<TaskResponse>> submitForApproval(
            @PathVariable UUID taskId,
            Authentication auth) {

        UUID fieldOfficerId = ((User) auth.getPrincipal()).getId();
        return ResponseEntity.ok(new ApiResponse<>(true, "Task submitted for approval",
                taskService.submitTaskForApproval(taskId, fieldOfficerId)));
    }

    @PostMapping("/{taskId}/approve-by-pm")
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    @Operation(summary = "Approve task by project manager")
    public ResponseEntity<ApiResponse<TaskResponse>> approveByProjectManager(
            @PathVariable UUID taskId,
            @Valid @RequestBody ApproveTaskRequest request,
            Authentication auth) {

        UUID projectManagerId = ((User) auth.getPrincipal()).getId();
        return ResponseEntity.ok(new ApiResponse<>(true, "Task approved by project manager",
                taskService.approveTaskByProjectManager(taskId, request.getApprovalNotes(), projectManagerId)));
    }

    @PostMapping("/{taskId}/reject-by-pm")
    @PreAuthorize("hasRole('PROJECT_MANAGER')")
    @Operation(summary = "Reject task by project manager")
    public ResponseEntity<ApiResponse<TaskResponse>> rejectByProjectManager(
            @PathVariable UUID taskId,
            @Valid @RequestBody RejectTaskRequest request,
            Authentication auth) {

        UUID projectManagerId = ((User) auth.getPrincipal()).getId();
        return ResponseEntity.ok(new ApiResponse<>(true, "Task rejected by project manager",
                taskService.rejectTaskByProjectManager(taskId, request.getRejectionReason(), projectManagerId)));
    }

    @PostMapping("/{taskId}/approve-by-finance")
    @PreAuthorize("hasRole('FINANCE')")
    @Operation(summary = "Approve task by finance team")
    public ResponseEntity<ApiResponse<TaskResponse>> approveByFinance(
            @PathVariable UUID taskId,
            @Valid @RequestBody ApproveTaskRequest request,
            Authentication auth) {

        UUID financeTeamMemberId = ((User) auth.getPrincipal()).getId();
        return ResponseEntity.ok(new ApiResponse<>(true, "Task approved by finance",
                taskService.approveTaskByFinance(taskId, request.getApprovalNotes(), financeTeamMemberId)));
    }

    @PostMapping("/{taskId}/reject-by-finance")
    @PreAuthorize("hasRole('FINANCE')")
    @Operation(summary = "Reject task by finance team")
    public ResponseEntity<ApiResponse<TaskResponse>> rejectByFinance(
            @PathVariable UUID taskId,
            @Valid @RequestBody RejectTaskRequest request,
            Authentication auth) {

        UUID financeTeamMemberId = ((User) auth.getPrincipal()).getId();
        return ResponseEntity.ok(new ApiResponse<>(true, "Task rejected by finance",
                taskService.rejectTaskByFinance(taskId, request.getRejectionReason(), financeTeamMemberId)));
    }

    @PostMapping("/{taskId}/approve-by-executive")
    @PreAuthorize("hasRole('EXECUTIVE_DIRECTOR')")
    @Operation(summary = "Approve task by executive director")
    public ResponseEntity<ApiResponse<TaskResponse>> approveByExecutive(
            @PathVariable UUID taskId,
            @Valid @RequestBody ApproveTaskRequest request,
            Authentication auth) {

        UUID executiveDirectorId = ((User) auth.getPrincipal()).getId();
        return ResponseEntity.ok(new ApiResponse<>(true, "Task approved by executive director",
                taskService.approveTaskByExecutive(taskId, request.getApprovalNotes(), executiveDirectorId)));
    }

    @PostMapping("/{taskId}/reject-by-executive")
    @PreAuthorize("hasRole('EXECUTIVE_DIRECTOR')")
    @Operation(summary = "Reject task by executive director")
    public ResponseEntity<ApiResponse<TaskResponse>> rejectByExecutive(
            @PathVariable UUID taskId,
            @Valid @RequestBody RejectTaskRequest request,
            Authentication auth) {

        UUID executiveDirectorId = ((User) auth.getPrincipal()).getId();
        return ResponseEntity.ok(new ApiResponse<>(true, "Task rejected by executive director",
                taskService.rejectTaskByExecutive(taskId, request.getRejectionReason(), executiveDirectorId)));
    }
}