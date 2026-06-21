package com.womenconcern.api.leave.service;

import com.womenconcern.api.leave.dto.LeaveTypeDto;
import com.womenconcern.api.utils.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ILeaveTypeService {

    LeaveTypeDto.Output createLeaveType(LeaveTypeDto.Input input);

    LeaveTypeDto.Output updateLeaveType(UUID id, LeaveTypeDto.Input input);

    LeaveTypeDto.Output getLeaveTypeById(UUID id);

    PageResponse<LeaveTypeDto.Output> getAllLeaveTypes(Pageable pageable);

    List<LeaveTypeDto.Output> getAvailableLeaveTypes(UUID userId);

    void deleteLeaveType(UUID id);

}
