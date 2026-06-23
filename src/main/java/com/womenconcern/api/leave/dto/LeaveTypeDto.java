package com.womenconcern.api.leave.dto;

import jakarta.validation.constraints.*;

import java.time.Instant;
import java.util.UUID;

public class LeaveTypeDto {
    public record Input(

            @NotBlank(message = "Leave type name is required")
            @Size(max = 100, message = "Name must not exceed 100 characters")
            String name,

            @Size(max = 255, message = "Description must not exceed 255 characters")
            String description,

            @NotNull(message = "Max days per year is required")
            @Min(value = 1, message = "Max days must be at least 1")
            @Max(value = 365, message = "Max days cannot exceed 365")
            Integer maxDaysPerYear,

            @NotNull(message = "Attachment requirement must be specified")
            Boolean requiresAttachment,

            @NotNull(message = "Paid flag is required")
            Boolean isPaid
    ) {}

    public record Output(

            UUID id,

            String name,

            String description,

            Integer maxDaysPerYear,

            Boolean requiresAttachment,

            Boolean isPaid,

            Instant createdAt,

            Instant updatedAt

    ) {}

}
