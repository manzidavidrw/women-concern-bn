package com.womenconcern.api.leave.entity;

import com.womenconcern.api.shared.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
}