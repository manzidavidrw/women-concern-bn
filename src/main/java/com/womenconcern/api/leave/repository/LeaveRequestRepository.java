package com.womenconcern.api.leave.repository;

import com.womenconcern.api.leave.entity.LeaveRequest;
import com.womenconcern.api.leave.leaveEnum.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, UUID> {
    List<LeaveRequest> findByEmployeeId(String employeeId);
    List<LeaveRequest> findByStatus(LeaveStatus status);

    @Query("""
    SELECT r FROM LeaveRequest r
    WHERE (:employeeId IS NULL OR r.employeeId = :employeeId)
    AND (:status IS NULL OR r.status = :status)
    """)
    List<LeaveRequest> findLeaveRequests(
            @Param("employeeId") String employeeId,
            @Param("status") LeaveStatus status
    );
}
