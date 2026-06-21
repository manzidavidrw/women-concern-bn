package com.womenconcern.api.leave.entity;

import com.womenconcern.api.auth.entity.User;
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false,columnDefinition = "uuid")
    private User employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leave_type_id", nullable = false,columnDefinition = "uuid")
    private LeaveType leaveType;

    @Column(name = "year", nullable = false)
    private Integer year;

    @Column(name = "allocated_days", nullable = false)
    private Integer allocatedDays;

    @Builder.Default
    @Column(name = "used_days", nullable = false)
    private Integer usedDays = 0;

    @Builder.Default
    @Column(name = "carried_forward", nullable =false)
    private Integer carriedForward = 0;

    @Column(name = "carry_expiry_date")
    private LocalDate carryExpiryDate;

    @PrePersist
    public void prePersist() {
        if (usedDays == null) usedDays = 0;
        if (carriedForward == null) carriedForward = 0;
    }

    @Transient
    public int getRemainingDays() {
        int allocated = allocatedDays == null ? 0 : allocatedDays;
        int carried = carriedForward == null ? 0 : carriedForward;
        int used = usedDays == null ? 0 : usedDays;

        return Math.max(allocated + carried - used, 0);
    }

    public void useDays(int days) {
        this.usedDays += days;
    }

    public void restoreDays(int days) {
        this.usedDays -= days;
    }
}