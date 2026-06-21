package com.womenconcern.api.leave.controller;

import com.womenconcern.api.auth.entity.User;
import com.womenconcern.api.leave.dto.LeaveTypeDto;
import com.womenconcern.api.leave.repository.LeaveTypeRepository;
import com.womenconcern.api.leave.service.ILeaveTypeService;
import com.womenconcern.api.utils.ApiResponse;
import com.womenconcern.api.utils.AuthUtils;
import com.womenconcern.api.utils.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/api/v1/leaveTypes")
@RequiredArgsConstructor
public class LeaveTypeController {

    private final ILeaveTypeService leaveTypeService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('EXECUTIVE_DIRECTOR')")
    public ResponseEntity<ApiResponse<LeaveTypeDto.Output>> createLeaveType(@RequestBody LeaveTypeDto.Input input){
        var  response = leaveTypeService.createLeaveType(input);
        ApiResponse<LeaveTypeDto.Output>  apiResponse = new ApiResponse<>(true, "Leave type created successfully", response );
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/leaders")
    @PreAuthorize("hasAnyRole('ADMIN','EXECUTIVE_DIRECTOR')")
    public ResponseEntity<ApiResponse<PageResponse<LeaveTypeDto.Output>>> getAllLeaveTypes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {

        Pageable pageable = PageRequest.of(page, size);

        PageResponse<LeaveTypeDto.Output> response = leaveTypeService.getAllLeaveTypes(pageable);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Leave types fetched successfully", response)
        );
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<LeaveTypeDto.Output>>> getLeaveTypes() {

        User user = AuthUtils.getCurrentUser();
        UUID userId = user.getId();

        return ResponseEntity.ok(
                new ApiResponse<>(
                        true,
                        "Leave types retrieved successfully",
                        leaveTypeService.getAvailableLeaveTypes(userId)
                )
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LeaveTypeDto.Output> getLeaveTypeById(@PathVariable UUID id) {
        var  response = leaveTypeService.getLeaveTypeById(id);
        ApiResponse<LeaveTypeDto.Output>  apiResponse = new ApiResponse<>(true, "Leave type retrieved successfully", response );
        return ResponseEntity.status(HttpStatus.CREATED) .body(apiResponse.getData());
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','EXECUTIVE_DIRECTOR')")
    public ResponseEntity<ApiResponse<LeaveTypeDto.Output>> updateLeaveType(@PathVariable UUID id, @RequestBody LeaveTypeDto.Input input){
        var response = leaveTypeService.updateLeaveType(id, input);
        ApiResponse<LeaveTypeDto.Output>  apiResponse = new ApiResponse<>(true, "Leave type updated successfully", response );
        return ResponseEntity.ok(apiResponse);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','EXECUTIVE_DIRECTOR')")
    public ResponseEntity<ApiResponse<Void>> deleteLeaveType(@PathVariable UUID id) {

        leaveTypeService.deleteLeaveType(id);

        ApiResponse<Void> response = new ApiResponse<>(
                true,
                "Leave type deleted successfully",
                null
        );

        return ResponseEntity.ok(response);
    }

}
