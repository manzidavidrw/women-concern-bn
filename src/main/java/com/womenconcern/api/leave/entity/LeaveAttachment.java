package com.womenconcern.api.leave.entity;

import com.womenconcern.api.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "leave_attachments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveAttachment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leave_request_id", nullable = false,columnDefinition = "uuid")
    private LeaveRequest leaveRequest;

    @Column(nullable = false, length = 1000)
    private String fileUrl;

    @Column(nullable = false)
    private String publicId;

    private String originalFileName;

    private String contentType;

    private Long fileSize;
}