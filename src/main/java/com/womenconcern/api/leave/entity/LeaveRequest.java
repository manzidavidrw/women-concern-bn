package com.womenconcern.api.leave.entity;

import com.womenconcern.api.leave.leaveEnum.LeaveStatus;
import com.womenconcern.api.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "leave_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveRequest extends BaseEntity {

    @Column(name = "employee_id", nullable = false)
    private String employeeId; // Keycloak user ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leave_type_id", nullable = false)
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

    // 👇 unified decision fields (approve OR reject)
    @Column(name = "decision_by")
    private String decisionById; // Keycloak user ID (manager/admin)

    @Column(name = "decision_at")
    private LocalDateTime decisionAt;

    @Column(name = "decision_comment")
    private String decisionComment;
}