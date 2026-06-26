package com.womenconcern.api.leave.repository;

import com.womenconcern.api.leave.entity.LeaveBalance;
import com.womenconcern.api.leave.entity.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, UUID> {

    Optional<LeaveBalance> findByEmployeeIdAndLeaveTypeIdAndYear(
            UUID employeeId,
            UUID leaveTypeId,
            Integer year
    );


    List<LeaveBalance> findByEmployeeId(UUID employeeId);

    List<LeaveBalance> findByEmployeeIdAndYear(UUID employeeId, Integer year);

    List<LeaveBalance> findByLeaveType(LeaveType leaveType);

    boolean existsByEmployeeIdAndLeaveTypeIdAndYear(
            UUID employeeId,
            UUID leaveTypeId,
            Integer year
    );
}