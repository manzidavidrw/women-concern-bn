package com.womenconcern.api.leave.controller;

import com.womenconcern.api.leave.dto.LeaveBalanceDto;
import com.womenconcern.api.leave.service.impl.LeaveBalanceServiceImpl;
import com.womenconcern.api.utils.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/leave-balance")
@RequiredArgsConstructor
public class LeaveBalanceController {

    private final LeaveBalanceServiceImpl leaveBalanceServiceImpl;

//    @GetMapping
//    public ResponseEntity<ApiResponse<List<LeaveBalanceDto.Output>>> getLeaveBalance(){
////            leaveBalanceServiceImpl.getMyBalances();
//    }
}
