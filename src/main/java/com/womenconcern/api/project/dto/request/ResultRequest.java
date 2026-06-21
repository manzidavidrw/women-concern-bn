package com.womenconcern.api.project.dto.request;


import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResultRequest {

    @NotBlank(message = "Result title is required")
    private String title;

    private String description;

}
