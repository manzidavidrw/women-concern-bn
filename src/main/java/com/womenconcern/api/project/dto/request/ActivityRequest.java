package com.womenconcern.api.project.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityRequest {

    @NotBlank(message = "Activity title is required")
    private String title;

    private String description;

    @DecimalMin(value = "0.0", inclusive = false, message = "Cost estimate must be greater than 0")
    private BigDecimal costEstimate;
}