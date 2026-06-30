package com.womenconcern.api.leave.entity;

import com.womenconcern.api.auth.entity.User;
import com.womenconcern.api.leave.leaveEnum.LeaveStatus;
import com.womenconcern.api.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "leave_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveRequest extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false,columnDefinition = "uuid")
    private User employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leave_type_id", nullable = false,columnDefinition = "uuid")
    private LeaveType leaveType;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "days_requested", nullable = false)
    private Integer daysRequested;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeaveStatus status;

    @OneToMany(
            mappedBy = "leaveRequest",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<LeaveAttachment> attachments = new ArrayList<>();

    // 👇 unified decision fields (approve OR reject)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "decision_by")
    private User decisionBy;

    @Column(name = "decision_at")
    private LocalDateTime decisionAt;

    @Column(name = "decision_comment")
    private String decisionComment;
}