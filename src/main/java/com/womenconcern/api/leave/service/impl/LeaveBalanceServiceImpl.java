package com.womenconcern.api.leave.service.impl;

import com.womenconcern.api.leave.dto.LeaveBalanceDto;
import com.womenconcern.api.leave.entity.LeaveBalance;
import com.womenconcern.api.leave.entity.LeaveType;
import com.womenconcern.api.leave.repository.LeaveBalanceRepository;
import com.womenconcern.api.leave.repository.LeaveTypeRepository;
import com.womenconcern.api.leave.service.ILeaveBalanceService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class LeaveBalanceServiceImpl implements ILeaveBalanceService {

    private final LeaveBalanceRepository leaveBalanceRepository;
    private final LeaveTypeRepository leaveTypeRepository;

    @Override
    public LeaveBalanceDto.Output createBalance(LeaveBalanceDto.Input input) {

        if (leaveBalanceRepository.existsByEmployeeIdAndLeaveTypeIdAndYear(
                input.employeeId(),
                input.leaveTypeId(),
                input.year()
        )) {
            throw new IllegalArgumentException("Leave balance already exists");
        }

        LeaveType leaveType = leaveTypeRepository.findById(input.leaveTypeId())
                .orElseThrow(() -> new EntityNotFoundException("Leave type not found"));

        int carriedForward = input.carriedForward() == null ? 0 : input.carriedForward();

        int allocated = input.allocatedDays();

        int remaining = allocated + carriedForward;

        LeaveBalance balance = LeaveBalance.builder()
                .employeeId(input.employeeId())
                .leaveType(leaveType)
                .year(input.year())
                .allocatedDays(allocated)
                .usedDays(0)
                .carriedForward(carriedForward)
                .carryExpiryDate(input.carryExpiryDate())
                .build();

        return mapToOutput(leaveBalanceRepository.save(balance));
    }

    @Override
    @Transactional(readOnly = true)
    public LeaveBalanceDto.Output getBalance(String employeeId, UUID leaveTypeId, Integer year) {

        LeaveBalance balance = leaveBalanceRepository
                .findByEmployeeIdAndLeaveTypeIdAndYear(employeeId, leaveTypeId, year)
                .orElseThrow(() -> new EntityNotFoundException("Leave balance not found"));

        return mapToOutput(balance);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveBalanceDto.Output> getMyBalances(String employeeId) {
        return leaveBalanceRepository.findByEmployeeId(employeeId)
                .stream()
                .map(this::mapToOutput)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveBalanceDto.Output> getMyYearlyBalances(String employeeId, Integer year) {
        return leaveBalanceRepository.findByEmployeeIdAndYear(employeeId, year)
                .stream()
                .map(this::mapToOutput)
                .toList();
    }

    @Override
    public void consumeLeaveDays(String employeeId, UUID leaveTypeId, Integer year, Integer days) {

        LeaveBalance balance = leaveBalanceRepository
                .findByEmployeeIdAndLeaveTypeIdAndYear(employeeId, leaveTypeId, year)
                .orElseThrow(() -> new EntityNotFoundException("Leave balance not found"));

        if (balance.getRemainingDays() < days) {
            throw new IllegalArgumentException("Insufficient leave balance");
        }

        balance.setUsedDays(balance.getUsedDays() + days);
        leaveBalanceRepository.save(balance);
    }

    @Override
    public void restoreLeaveDays(String employeeId, UUID leaveTypeId, Integer year, Integer days) {

        LeaveBalance balance = leaveBalanceRepository
                .findByEmployeeIdAndLeaveTypeIdAndYear(employeeId, leaveTypeId, year)
                .orElseThrow(() -> new EntityNotFoundException("Leave balance not found"));

        int newUsed = balance.getUsedDays() - days;

        if (newUsed < 0) {
            throw new IllegalArgumentException("Used days cannot be negative");
        }

        balance.setUsedDays(newUsed);
        leaveBalanceRepository.save(balance);
    }

    private LeaveBalanceDto.Output mapToOutput(LeaveBalance balance) {
        return new LeaveBalanceDto.Output(
                balance.getId(),
                balance.getEmployeeId(),
                balance.getLeaveType().getId(),
                balance.getLeaveType().getName(),
                balance.getYear(),
                balance.getAllocatedDays(),
                balance.getUsedDays(),
                balance.getRemainingDays(),
                balance.getCarriedForward(),
                balance.getCarryExpiryDate(),
                balance.getCreatedAt(),
                balance.getUpdatedAt()
        );
    }
}
