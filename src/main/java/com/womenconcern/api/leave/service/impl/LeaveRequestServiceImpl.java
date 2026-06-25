package com.womenconcern.api.leave.service.impl;

import com.womenconcern.api.leave.dto.LeaveRequestDto;
import com.womenconcern.api.leave.dto.LeaveTypeDto;
import com.womenconcern.api.leave.entity.LeaveBalance;
import com.womenconcern.api.leave.entity.LeaveRequest;
import com.womenconcern.api.leave.entity.LeaveType;
import com.womenconcern.api.leave.leaveEnum.LeaveStatus;
import com.womenconcern.api.leave.repository.LeaveBalanceRepository;
import com.womenconcern.api.leave.repository.LeaveRequestRepository;
import com.womenconcern.api.leave.repository.LeaveTypeRepository;
import com.womenconcern.api.leave.service.ILeaveRequestService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaveRequestServiceImpl implements ILeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;

    @Override
    public LeaveRequestDto.Output createLeaveRequest(LeaveRequestDto.Input input) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // auth.getName() returns the subject/username set in your JwtAuthenticationFilter
        String employeeId = auth.getName();

        LeaveType leaveType = leaveTypeRepository.findById(input.leaveTypeId())
                .orElseThrow(() -> new EntityNotFoundException("Leave type not found"));

        long days = input.startDate()
                .datesUntil(input.endDate().plusDays(1))
                .filter(date ->
                        date.getDayOfWeek() != java.time.DayOfWeek.SATURDAY &&
                                date.getDayOfWeek() != java.time.DayOfWeek.SUNDAY
                )
                .count();

        if (days > leaveType.getMaxDaysPerYear()) {
            throw new IllegalArgumentException(
                    "Leave exceeds max allowed days for this leave type: " + leaveType.getMaxDaysPerYear()
            );
        }

        LeaveBalance balance = leaveBalanceRepository
                .findByEmployeeIdAndLeaveTypeIdAndYear(
                        employeeId,
                        leaveType.getId(),
                        java.time.Year.now().getValue()
                )
                .orElseThrow(() -> new IllegalArgumentException("Leave balance not found"));

        if (days > balance.getRemainingDays()) {
            throw new IllegalArgumentException(
                    "Insufficient leave balance. Remaining days: " + balance.getRemainingDays()
            );
        }

        LeaveRequest request = LeaveRequest.builder()
                .employeeId(employeeId)
                .leaveType(leaveType)
                .startDate(input.startDate())
                .endDate(input.endDate())
                .daysRequested((int) days)
                .reason(input.reason())
                .status(LeaveStatus.DRAFT)
                .build();

        LeaveRequest saved = leaveRequestRepository.save(request);

        return mapToOutput(saved);
    }

    @Override
    public List<LeaveRequestDto.Output> getMyRequests(String employeeId, LeaveStatus status) {
        return leaveRequestRepository.findLeaveRequests(employeeId, status)
                .stream()
                .map(this::mapToOutput)
                .toList();
    }

    @Override
    public List<LeaveRequestDto.Output> getAllLeaveRequests(String employeeId, LeaveStatus status) {
        return leaveRequestRepository.findLeaveRequests(employeeId, status)
                .stream()
                .filter(r -> r.getStatus() != LeaveStatus.DRAFT)
                .map(this::mapToOutput)
                .toList();
    }

    @Override
    public LeaveRequestDto.Output updateStatus(LeaveRequestDto.UpdateLeaveStatusRequest request) {
        return null;
    }

    private LeaveRequestDto.Output mapToOutput(LeaveRequest entity) {
        return new LeaveRequestDto.Output(
                entity.getId(),
                entity.getEmployeeId(),
                new LeaveTypeDto.Output(
                        entity.getLeaveType().getId(),
                        entity.getLeaveType().getName(),
                        entity.getLeaveType().getDescription(),
                        entity.getLeaveType().getMaxDaysPerYear(),
                        entity.getLeaveType().getRequiresAttachment(),
                        entity.getLeaveType().getIsPaid(),
                        entity.getLeaveType().getCreatedAt(),
                        entity.getLeaveType().getUpdatedAt()
                ),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getDaysRequested(),
                entity.getReason(),
                entity.getStatus(),
                entity.getDecisionById(),
                entity.getDecisionAt(),
                entity.getDecisionComment(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}