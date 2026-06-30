package com.womenconcern.api.leave.service;

import com.womenconcern.api.leave.dto.LeaveRequestDto;
import com.womenconcern.api.leave.entity.LeaveType;
import com.womenconcern.api.leave.leaveEnum.LeaveStatus;
import com.womenconcern.api.utils.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface ILeaveRequestService {

    // Create a new leave request
    LeaveRequestDto.Output createLeaveRequest(
            LeaveRequestDto.Input input,
            List<MultipartFile> attachments,
            UUID employeeId
    );

    LeaveRequestDto.Output submitRequest(UUID requestId, UUID employeeId);

    // Get current logged-in user's requests
    PageResponse<LeaveRequestDto.Output> getMyRequests(
            UUID employeeId,
            LeaveStatus status,
            Pageable pageable
    );

    // Admin/HR view all requests
    PageResponse<LeaveRequestDto.Output> getAllLeaveRequests(LeaveStatus status, Pageable pageable);

    // Optional but VERY useful
    LeaveRequestDto.Output getRequestById(UUID requestId);

    LeaveRequestDto.Output approveRequest(UUID requestId, UUID employeeId);

    LeaveRequestDto.Output rejectRequest(UUID requestId, UUID employeeId,  String comment);

    LeaveRequestDto.Output resubmitRequest(UUID requestId, UUID employeeId);

    LeaveRequestDto.Output cancelRequest(UUID requestId, UUID employeeId);
    void deleteLeaveRequest(UUID requestId, UUID employeeId);
}