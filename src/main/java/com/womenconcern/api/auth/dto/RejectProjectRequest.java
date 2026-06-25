package com.womenconcern.api.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RejectProjectRequest {

    @NotBlank(message = "Rejection reason is required")
    private String rejectionReason;
}