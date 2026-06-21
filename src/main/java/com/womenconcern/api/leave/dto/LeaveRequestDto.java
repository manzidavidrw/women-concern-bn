package com.womenconcern.api.leave.dto;

import com.womenconcern.api.leave.entity.LeaveType;
import com.womenconcern.api.leave.leaveEnum.LeaveStatus;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class LeaveRequestDto {

    public record Input(

            @NotNull(message = "Leave type is required")
            UUID leaveTypeId,

            @NotNull(message = "Start date is required")
            LocalDate startDate,

            @NotNull(message = "End date is required")
            LocalDate endDate,

            String reason
    ) {}

    public record Output(
            UUID id,

            String employeeId,

            LeaveTypeDto.Output leaveType,

            LocalDate startDate,
            LocalDate endDate,
            Integer daysRequested,

            String reason,

            LeaveStatus status,

            String decisionById,
            LocalDateTime decisionAt,
            String decisionComment,

            Instant createdAt,
            Instant updatedAt
    ) {}

    public record UpdateLeaveStatusRequest(
            UUID leaveRequestId,
            LeaveStatus status,
            String comment
    ) {}
}
