package com.womenconcern.api.leave.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class LeaveBalanceDto {
    public record Input(

            @NotNull(message = "Employee ID is required")
            String employeeId,

            @NotNull(message = "Leave type is required")
            UUID leaveTypeId,

            @NotNull(message = "Year is required")
            Integer year,

            @NotNull(message = "Allocated days are required")
            @Min(value = 0, message = "Allocated days cannot be negative")
            Integer allocatedDays,

            Integer carriedForward,

            LocalDate carryExpiryDate
    ) {}

    public record Output(

            UUID id,

            String employeeId,

            UUID leaveTypeId,
            String leaveTypeName,

            Integer year,

            Integer allocatedDays,

            Integer usedDays,

            Integer remainingDays,

            Integer carriedForward,

            LocalDate carryExpiryDate,

            Instant createdAt,
            Instant updatedAt


    ) {}
}
