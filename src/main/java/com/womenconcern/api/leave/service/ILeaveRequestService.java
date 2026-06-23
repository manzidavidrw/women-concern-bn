package com.womenconcern.api.leave.service;

import com.womenconcern.api.leave.dto.LeaveRequestDto;
import com.womenconcern.api.leave.entity.LeaveRequest;
import com.womenconcern.api.leave.leaveEnum.LeaveStatus;

import java.util.List;

public interface ILeaveRequestService{

    LeaveRequestDto.Output createLeaveRequest(LeaveRequestDto.Input input);

    List<LeaveRequestDto.Output> getMyRequests(String employeeId, LeaveStatus status);

    List<LeaveRequestDto.Output> getAllLeaveRequests( String employeeId, LeaveStatus status);

    LeaveRequestDto.Output updateStatus(LeaveRequestDto.UpdateLeaveStatusRequest request);

}
