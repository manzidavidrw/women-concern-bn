package com.womenconcern.api.project.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateActivityRequest {

    @Valid
    @NotEmpty(message = "At least one activity is required")
    private List<ActivityRequest> activities;
}