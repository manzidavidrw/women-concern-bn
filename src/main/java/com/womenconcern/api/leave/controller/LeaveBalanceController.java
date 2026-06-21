package com.womenconcern.api.leave.controller;

import com.womenconcern.api.auth.entity.User;
import com.womenconcern.api.leave.dto.LeaveBalanceDto;
import com.womenconcern.api.leave.service.ILeaveBalanceService;
import com.womenconcern.api.utils.ApiResponse;
import com.womenconcern.api.utils.AuthUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/leave-balances")
@RequiredArgsConstructor
public class LeaveBalanceController {

    private final ILeaveBalanceService leaveBalanceService;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<LeaveBalanceDto.Output>>> getMyBalances() {

        User user = AuthUtils.getCurrentUser();

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Leave balances retrieved successfully",
                        leaveBalanceService.getEmployeeBalances(user.getId())
                )
        );
    }

    @GetMapping("/{leaveTypeId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<LeaveBalanceDto.Output>> getBalance(
            @PathVariable UUID leaveTypeId) {

        User user = AuthUtils.getCurrentUser();


        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Leave balance retrieved successfully",
                        leaveBalanceService.getBalance(user.getId(), leaveTypeId)
                )
        );
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','EXECUTIVE_DIRECTOR')")
    public ResponseEntity<ApiResponse<List<LeaveBalanceDto.Output>>> getAllBalances() {

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Leave balances retrieved successfully",
                        leaveBalanceService.getAllBalances()
                )
        );
    }
}