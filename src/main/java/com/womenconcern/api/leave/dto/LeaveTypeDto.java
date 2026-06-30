package com.womenconcern.api.leave.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.womenconcern.api.leave.leaveEnum.LeaveEligibility;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.Instant;
import java.util.UUID;

public class LeaveTypeDto {
    public LeaveTypeDto() {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Schema(name = "LeaveType.Input")
    public record Input(

            @NotBlank(message = "Leave type name is required")
            @Size(max = 100, message = "Name must not exceed 100 characters")
            String name,

            @Size(max = 255, message = "Description must not exceed 255 characters")
            String description,

            @NotNull(message = "Maximum days per year is required")
            @Min(value = 1, message = "Maximum days must be at least 1")
            @Max(value = 365, message = "Maximum days cannot exceed 365")
            Integer maxDaysPerYear,

            @NotNull(message = "Attachment requirement must be specified")
            Boolean requiresAttachment,

            @NotNull(message = "Paid flag is required")
            Boolean isPaid,

            @NotNull(message = "Eligibility is required")
            LeaveEligibility eligibility,

            @NotNull(message = "Carry forward option must be specified")
            Boolean allowCarryForward,

            @Min(value = 0, message = "Maximum carry forward days cannot be negative")
            @Max(value = 10, message = "Maximum carry forward days cannot exceed 10")
            Integer maxCarryForwardDays

    ){}

    @Schema(name = "LeaveType.Output")
    public record Output(

            UUID id,

            String name,

            String description,

            Integer maxDaysPerYear,

            Boolean requiresAttachment,

            Boolean isPaid,

            LeaveEligibility eligibility,

            Boolean allowCarryForward,

            Integer maxCarryForwardDays,

            Instant createdAt,

            Instant updatedAt

    ) {}

}
