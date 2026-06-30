package com.womenconcern.api.leave.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.womenconcern.api.auth.dto.UserDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class LeaveBalanceDto {
    public LeaveBalanceDto() {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Schema(name = "LeaveBalanceDto.Input")
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

    @Schema(name = "LeaveBalanceDto.Output")
    public record Output(

            UUID id,

            UserDto employeeId,

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
