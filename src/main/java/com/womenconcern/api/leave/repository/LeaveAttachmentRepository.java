package com.womenconcern.api.leave.repository;

import com.womenconcern.api.leave.entity.LeaveAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LeaveAttachmentRepository extends JpaRepository<LeaveAttachment, UUID> {
}
