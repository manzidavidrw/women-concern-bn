package com.womenconcern.api.leave.service;

import com.womenconcern.api.leave.dto.LeaveBalanceDto;
import com.womenconcern.api.leave.entity.LeaveBalance;

import java.util.List;
import java.util.UUID;

public interface ILeaveBalanceService {

    LeaveBalanceDto.Output createBalance(LeaveBalanceDto.Input input);

    LeaveBalanceDto.Output getBalance(String employeeId, UUID leaveTypeId, Integer year);

    List<LeaveBalanceDto.Output> getMyBalances(String employeeId);

    List<LeaveBalanceDto.Output> getMyYearlyBalances(String employeeId, Integer year);

    void consumeLeaveDays(String employeeId, UUID leaveTypeId, Integer year, Integer days);

    void restoreLeaveDays(String employeeId, UUID leaveTypeId, Integer year, Integer days);

}