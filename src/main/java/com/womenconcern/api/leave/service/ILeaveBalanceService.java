package com.womenconcern.api.leave.service;

import com.womenconcern.api.leave.dto.LeaveBalanceDto;
import com.womenconcern.api.leave.entity.LeaveBalance;
import com.womenconcern.api.leave.entity.LeaveType;

import java.util.List;
import java.util.UUID;

public interface ILeaveBalanceService {

    /**
     * Returns the current balance for a leave type.
     * Creates it automatically if it doesn't exist.
     */
    LeaveBalanceDto.Output getBalance(UUID employeeId, UUID leaveTypeId);

    /**
     * Returns all leave balances for an employee.
     * Missing balances are created automatically.
     */
    List<LeaveBalanceDto.Output> getEmployeeBalances(UUID employeeId);

    /**
     * Returns all balances in the system.
     * Intended for HR/Admin.
     */
    List<LeaveBalanceDto.Output> getAllBalances();

    /**
     * Recalculates or creates the balance for a specific year.
     * Useful if HR changes a leave type allocation.
     */
    LeaveBalanceDto.Output refreshBalance(UUID employeeId, UUID leaveTypeId, Integer year);
    LeaveBalance getOrCreateBalance(UUID employeeId,
                                    LeaveType leaveType,
                                    int year);

    void deductLeave(UUID employeeId,
                     LeaveType leaveType,
                     int year,
                     int days);

}