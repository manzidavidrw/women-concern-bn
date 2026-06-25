package com.womenconcern.api.project.dto.request;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApproveTaskRequest {

    @NotBlank(message = "Approval notes are required")
    private String approvalNotes;
}