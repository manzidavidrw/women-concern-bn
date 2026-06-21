package com.womenconcern.api.leave.entity;

import com.womenconcern.api.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(
        name = "leave_balances",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"employee_id", "leave_type_id", "year"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveBalance extends BaseEntity {

    @Column(name = "employee_id", nullable = false)
    private String employeeId; // Keycloak user ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leave_type_id", nullable = false)
    private LeaveType leaveType;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "allocated_days", nullable = false)
    private Integer allocatedDays;

    @Column(name = "used_days", nullable = false)
    private Integer usedDays = 0;

    private Integer carriedForward;

    private LocalDate carryExpiryDate;

    public int getRemainingDays() {
        int carried = carriedForward == null ? 0 : carriedForward;
        int used = usedDays == null ? 0 : usedDays;
        return allocatedDays + carried - used;
    }
}