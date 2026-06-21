package com.womenconcern.api.project.dto.request;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
public class CreateOutcomeRequest {

    @Valid
    @NotEmpty(message = "At least one outcome is required")
    private List<OutcomeRequest> outcomes;
}
