package com.womenconcern.api.leave.repository;

import com.womenconcern.api.leave.entity.LeaveBalance;
import com.womenconcern.api.leave.entity.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LeaveBalanceRepository extends JpaRepository<LeaveBalance, UUID> {

    @Query(
            value = """
        SELECT * FROM leave_balances
        WHERE employee_id = CAST(:employeeId AS uuid)
          AND leave_type_id = CAST(:leaveTypeId AS uuid)
          AND year = :year
        LIMIT 1
    """,
            nativeQuery = true
    )
    Optional<LeaveBalance> findByEmployeeIdAndLeaveTypeIdAndYear(
            @Param("employeeId") UUID employeeId,
            @Param("leaveTypeId") UUID leaveTypeId,
            @Param("year") Integer year
    );

    @Query("""
        SELECT lb FROM LeaveBalance lb
        WHERE lb.employee.id = :employeeId
    """)
    List<LeaveBalance> findByEmployeeId(
            @Param("employeeId") UUID employeeId
    );

    @Query("""
        SELECT lb FROM LeaveBalance lb
        WHERE lb.employee.id = :employeeId
          AND lb.year = :year
    """)
    List<LeaveBalance> findByEmployeeIdAndYear(
            @Param("employeeId") UUID employeeId,
            @Param("year") Integer year
    );

    @Query("""
        SELECT lb FROM LeaveBalance lb
        WHERE lb.leaveType = :leaveType
    """)
    List<LeaveBalance> findByLeaveType(
            @Param("leaveType") LeaveType leaveType
    );

    @Query("""
        SELECT CASE WHEN COUNT(lb) > 0 THEN true ELSE false END
        FROM LeaveBalance lb
        WHERE lb.employee.id = :employeeId
          AND lb.leaveType.id = :leaveTypeId
          AND lb.year = :year
    """)
    boolean existsByEmployeeIdAndLeaveTypeIdAndYear(
            @Param("employeeId") UUID employeeId,
            @Param("leaveTypeId") UUID leaveTypeId,
            @Param("year") Integer year
    );
}