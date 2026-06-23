package com.womenconcern.api.leave.controller;

import com.womenconcern.api.leave.dto.LeaveRequestDto;
import com.womenconcern.api.leave.service.impl.LeaveRequestServiceImpl;
import com.womenconcern.api.utils.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/leave-requests")
public class LeaveRequestController {

    private final LeaveRequestServiceImpl leaveRequestService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<LeaveRequestDto.Output>> createLeaveRequest(@RequestBody LeaveRequestDto.Input leaveRequestDto){
        LeaveRequestDto.Output response = leaveRequestService.createLeaveRequest(leaveRequestDto);
        ApiResponse<LeaveRequestDto.Output> apiResponse = new ApiResponse<>( true, "Leave request created successfully", response );
        return ResponseEntity.status(HttpStatus.CREATED) .body(apiResponse);
    }
}
