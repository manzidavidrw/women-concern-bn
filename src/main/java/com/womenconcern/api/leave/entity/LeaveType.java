package com.womenconcern.api.leave.entity;

import com.womenconcern.api.leave.leaveEnum.LeaveEligibility;
import com.womenconcern.api.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "leave_types")
@Getter
@Setter
public class LeaveType extends BaseEntity {

    private String name;

    private String description;

    private Integer maxDaysPerYear;

    private Boolean requiresAttachment;

    private Boolean isPaid;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeaveEligibility eligibility = LeaveEligibility.ALL;

    private Boolean allowCarryForward;

    private Integer maxCarryForwardDays;
}