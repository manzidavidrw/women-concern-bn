package com.womenconcern.api.leave.repository;

import com.womenconcern.api.leave.entity.LeaveRequest;
import com.womenconcern.api.leave.leaveEnum.LeaveStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, UUID> {
    Page<LeaveRequest> findByEmployeeId(UUID employeeId, Pageable pageable);

    Page<LeaveRequest> findByStatus(LeaveStatus status, Pageable pageable);

    Page<LeaveRequest> findAll(Pageable pageable);

    @Query("""
    SELECT r FROM LeaveRequest r
    WHERE r.status <> com.womenconcern.api.leave.leaveEnum.LeaveStatus.DRAFT
    AND (:status IS NULL OR r.status = :status)
""")
    Page<LeaveRequest> findAllNonDraft(
            @Param("status") LeaveStatus status,
            Pageable pageable
    );

    @Query("""
SELECT r FROM LeaveRequest r
WHERE (:employeeId IS NULL OR r.employee.id = :employeeId)
AND (:status IS NULL OR r.status = :status)
""")
    Page<LeaveRequest>  findByEmployeeIdAndStatus(
            @Param("employeeId") UUID employeeId,
            @Param("status") LeaveStatus status,
            Pageable pageable
    );

}
