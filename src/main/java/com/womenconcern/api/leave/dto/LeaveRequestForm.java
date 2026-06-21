package com.womenconcern.api.leave.dto;

import com.womenconcern.api.leave.leaveEnum.LeaveRequestAction;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class LeaveRequestForm {

    @NotNull(message = "Leave type is required")
    private UUID leaveTypeId;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    private LeaveRequestAction action;

    private String reason;
}