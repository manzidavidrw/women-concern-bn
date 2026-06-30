package com.womenconcern.api.leave.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.womenconcern.api.auth.entity.User;
import com.womenconcern.api.leave.dto.LeaveRequestDto;
import com.womenconcern.api.leave.leaveEnum.LeaveStatus;
import com.womenconcern.api.leave.service.ILeaveRequestService;
import com.womenconcern.api.leave.dto.LeaveRequestForm;
import com.womenconcern.api.leave.leaveEnum.LeaveStatus;
import com.womenconcern.api.leave.service.ILeaveRequestService;
import com.womenconcern.api.utils.ApiResponse;
import com.womenconcern.api.utils.AuthUtils;
import com.womenconcern.api.utils.PageResponse;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/leave-requests")
public class LeaveRequestController {

    private final ILeaveRequestService leaveRequestService;

    // ─────────────────────────────────────────────
    // CREATE LEAVE REQUEST
    // ─────────────────────────────────────────────
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<LeaveRequestDto.Output>> createLeaveRequest(

            @ParameterObject @ModelAttribute @Valid LeaveRequestForm form,

            @RequestPart(value = "attachments", required = false)
            List<MultipartFile> attachments
    ) {

        User user = AuthUtils.getCurrentUser();

        LeaveRequestDto.Input request = new LeaveRequestDto.Input(
                form.getLeaveTypeId(),
                form.getStartDate(),
                form.getEndDate(),
                form.getAction(),
                form.getReason()
        );

        LeaveRequestDto.Output response =
                leaveRequestService.createLeaveRequest(request, attachments, user.getId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Leave request created successfully", response));
    }

    // ─────────────────────────────────────────────
    // GET MY REQUESTS
    // ─────────────────────────────────────────────
    @PostMapping("/{id}/submit")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<LeaveRequestDto.Output>> submitLeaveRequest(
            @PathVariable UUID id
    ) {

        User user = AuthUtils.getCurrentUser();

        LeaveRequestDto.Output response =
                leaveRequestService.submitRequest(id, user.getId());

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Leave request submitted successfully",
                        response
                )
        );
    }

    // ─────────────────────────────────────────────
    // GET MY REQUESTS
    // ─────────────────────────────────────────────
    @GetMapping("/my-requests")
    public ResponseEntity<ApiResponse<PageResponse<LeaveRequestDto.Output>>> getMyRequests(
            @RequestParam(required = false) LeaveStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {

        Pageable pageable = PageRequest.of(page, size);
        User user = AuthUtils.getCurrentUser();

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Leave requests found",
                        leaveRequestService.getMyRequests(user.getId(), status, pageable)
                )
        );
    }

    // ─────────────────────────────────────────────
    // GET SINGLE REQUEST
    // ─────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LeaveRequestDto.Output>> getRequestById(
            @PathVariable UUID id) {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Leave request found",
                        leaveRequestService.getRequestById(id)
                )
        );
    }

    // =========================
    // EMPLOYEE: RESUBMIT REQUEST
    // =========================

    @PostMapping("/{id}/resubmit")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<LeaveRequestDto.Output>> resubmit(
            @PathVariable UUID id
    ) {
        User user = AuthUtils.getCurrentUser();

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Leave request resubmitted successfully",
                        leaveRequestService.resubmitRequest(id, user.getId())
                )
        );
    }

    // =========================
    // EMPLOYEE: CANCEL REQUEST
    // =========================
    @PostMapping("/{id}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<LeaveRequestDto.Output>> cancelRequest(
            @PathVariable UUID id
    ) {
        User user = AuthUtils.getCurrentUser();

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Leave request cancelled successfully",
                        leaveRequestService.cancelRequest(id, user.getId())
                )
        );
    }

    // ─────────────────────────────────────────────
    // ADMIN / HR: GET ALL REQUESTS
    // ─────────────────────────────────────────────
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EXECUTIVE_DIRECTOR')")
    public ResponseEntity<ApiResponse<PageResponse<LeaveRequestDto.Output>>> getAllLeaveRequests(
            @RequestParam(required = false) LeaveStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "All leave requests retrieved",
                        leaveRequestService.getAllLeaveRequests(status, pageable)
                )
        );
    }

    // =========================
    // EXECUTIVE DIRECTOR / ADMIN: APPROVE
    // =========================
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('EXECUTIVE_DIRECTOR','ADMIN')")
    public ResponseEntity<ApiResponse<LeaveRequestDto.Output>> approveRequest(
            @PathVariable UUID id
    ) {
        User actor = AuthUtils.getCurrentUser();

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Leave request approved successfully",
                        leaveRequestService.approveRequest(id, actor.getId())
                )
        );
    }

    // =========================
    // EXECUTIVE DIRECTOR / ADMIN: REJECT
    // =========================
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('EXECUTIVE_DIRECTOR','ADMIN')")
    public ResponseEntity<ApiResponse<LeaveRequestDto.Output>> rejectRequest(
            @PathVariable UUID id,
            @RequestParam String comment
    ) {
        User actor = AuthUtils.getCurrentUser();

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Leave request rejected successfully",
                        leaveRequestService.rejectRequest(id, actor.getId(), comment)
                )
        );
    }

    // =========================
    // DELETE LEAVE REQUEST
    // =========================
    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteLeaveRequest(
            @PathVariable UUID id
    ) {

        User user = AuthUtils.getCurrentUser();

        leaveRequestService.deleteLeaveRequest(id, user.getId());

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Leave request deleted successfully",
                        null
                )
        );
    }

}