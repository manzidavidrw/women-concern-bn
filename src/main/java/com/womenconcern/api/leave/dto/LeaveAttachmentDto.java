package com.womenconcern.api.leave.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

public class LeaveAttachmentDto {
    @Schema(name = "LeaveAttachment.Output")
    public record Output(
            UUID id,
            String fileUrl,
            String originalFileName,
            String contentType,
            String publicId,
            Long fileSize,
            Instant createdAt
    ) {
    }
}
