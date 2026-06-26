package com.womenconcern.api.leave.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.womenconcern.api.auth.dto.UserDto;
import com.womenconcern.api.leave.leaveEnum.LeaveRequestAction;
import com.womenconcern.api.leave.leaveEnum.LeaveStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class LeaveRequestDto {
    public LeaveRequestDto() {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Schema(name = "LeaveRequest.Input")
    public record Input(

            @NotNull(message = "Leave type is required")
            UUID leaveTypeId,

            @NotNull(message = "Start date is required")
            LocalDate startDate,

            @NotNull(message = "End date is required")
            LocalDate endDate,

            LeaveRequestAction action,

            String reason
    ) {}

    @Schema(name = "LeaveRequest.Output")
    public record Output(
            UUID id,

            UserDto employeeId,

            LeaveTypeDto.Output leaveType,

            LocalDate startDate,
            LocalDate endDate,
            Integer daysRequested,

            String reason,

            LeaveStatus status,

            UserDto decisionById,
            LocalDateTime decisionAt,
            String decisionComment,
            List<LeaveAttachmentDto.Output> attachments,

            Instant createdAt,
            Instant updatedAt
    ) {}
    @Schema(name = "LeaveRequest.UpdatePayload")
    public record UpdateLeaveStatusRequest(
            UUID leaveRequestId,
            LeaveStatus status,
            String comment
    ) {}
}
