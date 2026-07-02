package com.womenconcern.api.leave.service.impl;

import com.womenconcern.api.auth.dto.UserDto;
import com.womenconcern.api.auth.entity.User;
import com.womenconcern.api.auth.mapper.UserMapper;
import com.womenconcern.api.auth.repository.UserRepository;
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

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class LeaveBalanceServiceImpl implements ILeaveBalanceService {

    private final LeaveBalanceRepository leaveBalanceRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final UserRepository userRepository;

    @Override
    public LeaveBalanceDto.Output getBalance(UUID employeeId, UUID leaveTypeId) {

        int year = java.time.Year.now().getValue();

        LeaveType leaveType = leaveTypeRepository.findById(leaveTypeId)
                .orElseThrow(() -> new EntityNotFoundException("Leave type not found"));

        LeaveBalance balance = getOrCreateBalance(employeeId, leaveType, year);

        return mapToOutput(balance);
    }

    @Override
    public List<LeaveBalanceDto.Output> getEmployeeBalances(UUID employeeId) {

        int year = java.time.Year.now().getValue();

        return leaveTypeRepository.findAll()
                .stream()
                .map(type -> getOrCreateBalance(employeeId, type, year))
                .map(this::mapToOutput)
                .toList();
    }

    @Override
    public List<LeaveBalanceDto.Output> getAllBalances() {

        return leaveBalanceRepository.findAll()
                .stream()
                .map(this::mapToOutput)
                .toList();
    }

    @Override
    public LeaveBalanceDto.Output refreshBalance(UUID employeeId,
                                                 UUID leaveTypeId,
                                                 Integer year) {

        LeaveType leaveType = leaveTypeRepository.findById(leaveTypeId)
                .orElseThrow(() -> new EntityNotFoundException("Leave type not found"));

        LeaveBalance balance = getOrCreateBalance(employeeId, leaveType, year);

        balance.setAllocatedDays(leaveType.getMaxDaysPerYear());

        leaveBalanceRepository.save(balance);

        return mapToOutput(balance);
    }


    @Override
    public LeaveBalance getOrCreateBalance(UUID employeeId,
                                           LeaveType leaveType,
                                           int year) {

        LeaveBalance balance = leaveBalanceRepository
                .findByEmployeeIdAndLeaveTypeIdAndYear(
                        employeeId,
                        leaveType.getId(),
                        year
                )
                .orElseGet(() -> createNewBalance(employeeId, leaveType, year));

        return refreshBalanceIfNeeded(balance);
    }

    private LeaveBalance createNewBalance(UUID employeeId,
                                          LeaveType leaveType,
                                          int year) {

        User employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found"));

        return leaveBalanceRepository.save(
                LeaveBalance.builder()
                        .employee(employee)
                        .leaveType(leaveType)
                        .year(year)
                        .allocatedDays(leaveType.getMaxDaysPerYear())
                        .usedDays(0)
                        .carriedForward(0)
                        .build()
        );
    }

    @Override
    public void deductLeave(UUID employeeId,
                            LeaveType leaveType,
                            int year,
                            int days) {

        LeaveBalance balance = getOrCreateBalance(employeeId, leaveType, year);

        int available = calculateAvailable(balance);

        if (available < days) {
            throw new IllegalStateException("Insufficient balance. Available: " + available);
        }

        applyDeduction(balance, days);

        leaveBalanceRepository.save(balance);
    }

    private LeaveBalance refreshBalanceIfNeeded(LeaveBalance balance) {

        LocalDate now = LocalDate.now();
        int currentYear = now.getYear();

        if (balance.getYear() < currentYear) {

            boolean isAfterFebruary = now.getMonthValue() > 2;

            int validCarryForward = isAfterFebruary ? 0 : balance.getCarriedForward();

            balance.setYear(currentYear);
            balance.setAllocatedDays(balance.getLeaveType().getMaxDaysPerYear());
            balance.setUsedDays(0);
            balance.setCarriedForward(validCarryForward);

            leaveBalanceRepository.save(balance);
        }

        return balance;
    }


    private void applyDeduction(LeaveBalance balance, int days) {

        int remaining = days;

        int carry = balance.getCarriedForward();

        int usedCarry = Math.min(carry, remaining);
        balance.setCarriedForward(carry - usedCarry);
        remaining -= usedCarry;

        if (remaining > 0) {
            balance.setUsedDays(balance.getUsedDays() + remaining);
        }
    }

    private int calculateAvailable(LeaveBalance balance) {
        return balance.getAllocatedDays()
                + balance.getCarriedForward()
                - balance.getUsedDays();
    }


    private LeaveBalanceDto.Output mapToOutput(LeaveBalance balance) {
        return new LeaveBalanceDto.Output(
                balance.getId(),
                UserMapper.mapToLinkedUser(balance.getEmployee()),
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
