package com.womenconcern.api.leave.service.impl;

import com.womenconcern.api.auth.enums.Gender;
import com.womenconcern.api.auth.dto.UserDto;
import com.womenconcern.api.auth.entity.User;
import com.womenconcern.api.auth.mapper.UserMapper;
import com.womenconcern.api.auth.repository.UserRepository;
import com.womenconcern.api.common.storage.dto.UploadedFile;
import com.womenconcern.api.common.storage.service.IFileStorageService;
import com.womenconcern.api.exception.ResourceNotFoundException;
import com.womenconcern.api.leave.dto.LeaveAttachmentDto;
import com.womenconcern.api.leave.dto.LeaveRequestDto;
import com.womenconcern.api.leave.dto.LeaveTypeDto;
import com.womenconcern.api.leave.entity.LeaveAttachment;
import com.womenconcern.api.leave.entity.LeaveBalance;
import com.womenconcern.api.leave.entity.LeaveRequest;
import com.womenconcern.api.leave.entity.LeaveType;
import com.womenconcern.api.leave.leaveEnum.LeaveRequestAction;
import com.womenconcern.api.leave.leaveEnum.LeaveStatus;
import com.womenconcern.api.leave.repository.LeaveAttachmentRepository;
import com.womenconcern.api.leave.repository.LeaveRequestRepository;
import com.womenconcern.api.leave.repository.LeaveTypeRepository;
import com.womenconcern.api.leave.service.ILeaveBalanceService;
import com.womenconcern.api.leave.service.ILeaveRequestService;
import com.womenconcern.api.utils.PageResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LeaveRequestServiceImpl implements ILeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveTypeRepository leaveTypeRepository;
    private final ILeaveBalanceService leaveBalanceService;
    private final UserRepository  userRepository;
    private final IFileStorageService  fileStorageService;
    private final LeaveAttachmentRepository leaveAttachmentRepository;

    @Override
    @Transactional
    public LeaveRequestDto.Output createLeaveRequest(
            LeaveRequestDto.Input input,
            List<MultipartFile> attachments,
            UUID employeeId
    ) {

        validateLeaveDates(input.startDate(), input.endDate());

        LeaveStatus status = (input.action() == LeaveRequestAction.SUBMIT)
                ? LeaveStatus.SUBMITTED
                : LeaveStatus.DRAFT;

        User employee = userRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        LeaveType leaveType = leaveTypeRepository.findById(input.leaveTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("Leave type not found"));

        if (!isEligible(employee, leaveType)) {
            throw new IllegalArgumentException("You are not eligible for " + leaveType.getName());
        }

        long days = input.startDate()
                .datesUntil(input.endDate().plusDays(1))
                .filter(date ->
                        date.getDayOfWeek() != DayOfWeek.SATURDAY &&
                                date.getDayOfWeek() != DayOfWeek.SUNDAY
                )
                .count();

        int year = input.startDate().getYear();

        LeaveBalance balance = leaveBalanceService.getOrCreateBalance(
                employeeId,
                leaveType,
                year
        );

        if (days > balance.getRemainingDays()) {
            throw new IllegalArgumentException("Insufficient leave balance");
        }

        // 1. create request FIRST
        LeaveRequest request = LeaveRequest.builder()
                .employee(employee)
                .leaveType(leaveType)
                .startDate(input.startDate())
                .endDate(input.endDate())
                .status(status)
                .daysRequested((int) days)
                .reason(input.reason())
                .build();

        LeaveRequest savedRequest = leaveRequestRepository.save(request);

        // 2. upload attachments (ALL OR FAIL)
        if (attachments != null && !attachments.isEmpty()) {
            List<LeaveAttachment> files = attachments.stream()
                    .map(file -> {

                        UploadedFile uploaded = fileStorageService.upload(file);

                        return LeaveAttachment.builder()
                                .leaveRequest(savedRequest)
                                .fileUrl(uploaded.url())
                                .publicId(uploaded.publicId())
                                .originalFileName(file.getOriginalFilename())
                                .contentType(file.getContentType())
                                .fileSize(file.getSize())
                                .build();
                    })
                    .toList();

            leaveAttachmentRepository.saveAll(files);
        }

        return mapToOutput(savedRequest);
    }

    @Override
    public LeaveRequestDto.Output submitRequest(UUID requestId, UUID employeeId) {
        LeaveRequest request = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found"));

        if (!request.getEmployee().getId().equals(employeeId)) {
            throw new IllegalArgumentException("You can only submit your own leave request");
        }

        if (request.getStatus() != LeaveStatus.DRAFT) {
            throw new IllegalStateException("Only draft can be submitted");
        }

        request.setStatus(LeaveStatus.SUBMITTED);
        return mapToOutput(leaveRequestRepository.save(request));
    }

    @Override
    public PageResponse<LeaveRequestDto.Output> getMyRequests(
            UUID employeeId,
            LeaveStatus status,
            Pageable pageable
    ) {

        Page<LeaveRequest> pageResult;

        if (status == null) {
            pageResult = leaveRequestRepository.findByEmployeeId(employeeId, pageable);
        } else {
            pageResult = leaveRequestRepository.findByEmployeeIdAndStatus(employeeId, status, pageable);
        }

        Page<LeaveRequestDto.Output> mapped = pageResult.map(this::mapToOutput);

        return new PageResponse<>(
                mapped.getContent(),
                mapped.getNumber(),
                mapped.getSize(),
                mapped.getTotalElements(),
                mapped.getTotalPages(),
                mapped.isFirst(),
                mapped.isLast()
        );
    }

    @Override
    public PageResponse<LeaveRequestDto.Output> getAllLeaveRequests(
            LeaveStatus status,
            Pageable pageable
    ) {

        Page<LeaveRequest> pageResult =
                leaveRequestRepository.findAllNonDraft(status, pageable);

        Page<LeaveRequestDto.Output> mapped = pageResult.map(this::mapToOutput);

        return new PageResponse<>(
                mapped.getContent(),
                mapped.getNumber(),
                mapped.getSize(),
                mapped.getTotalElements(),
                mapped.getTotalPages(),
                mapped.isFirst(),
                mapped.isLast()
        );
    }

    @Override
    public LeaveRequestDto.Output getRequestById(UUID requestId) {

        LeaveRequest request = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found"));

        return mapToOutput(request);
    }

    @Override
    public LeaveRequestDto.Output approveRequest(UUID requestId, UUID actorId) {

        LeaveRequest request = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found"));

        User actor = userRepository.findById(actorId)
                .orElseThrow(() -> new ResourceNotFoundException("Actor not found"));

        validateStatus(request);

        leaveBalanceService.deductLeave(
                request.getEmployee().getId(),
                request.getLeaveType(),
                request.getStartDate().getYear(),
                request.getDaysRequested()
        );

        request.setStatus(LeaveStatus.APPROVED);
        request.setDecisionBy(actor);
        request.setDecisionAt(LocalDateTime.now());
        request.setDecisionComment("Approved");

        return mapToOutput(leaveRequestRepository.save(request));
    }

    @Override
    public LeaveRequestDto.Output rejectRequest(UUID requestId, UUID actorId, String comment) {

        LeaveRequest leaveRequest = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found"));

        User actor = userRepository.findById(actorId)
                .orElseThrow(() -> new ResourceNotFoundException("Actor not found"));

        // Only submitted requests can be rejected
        if (leaveRequest.getStatus() != LeaveStatus.SUBMITTED) {
            throw new IllegalStateException("Only SUBMITTED requests can be rejected");
        }

        leaveRequest.setStatus(LeaveStatus.REJECTED);
        leaveRequest.setDecisionBy(actor);
        leaveRequest.setDecisionAt(LocalDateTime.now());
        leaveRequest.setDecisionComment(comment);

        return mapToOutput(leaveRequestRepository.save(leaveRequest));
    }

    @Override
    public LeaveRequestDto.Output resubmitRequest(UUID requestId, UUID employeeId) {

        LeaveRequest request = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found"));

        validateLeaveDates(request.getStartDate(),  request.getEndDate());

        request.setStatus(LeaveStatus.SUBMITTED);
        request.setDecisionAt(null);
        request.setDecisionBy(null);
        request.setDecisionComment("Resubmitted by employee");

        return mapToOutput(leaveRequestRepository.save(request));
    }

    @Override
    public LeaveRequestDto.Output cancelRequest(UUID requestId, UUID employeeId) {

        LeaveRequest request = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found"));

        if (!request.getEmployee().getId().equals(employeeId)) {
            throw new IllegalArgumentException("You can only cancel your own request");
        }

        if (request.getStatus() != LeaveStatus.SUBMITTED &&
                request.getStatus() != LeaveStatus.DRAFT) {
            throw new IllegalStateException("Cannot cancel processed request");
        }

        request.setStatus(LeaveStatus.CANCELLED);
        request.setDecisionAt(LocalDateTime.now());
        request.setDecisionComment("Cancelled by employee");

        return mapToOutput(leaveRequestRepository.save(request));
    }

    @Override
    @Transactional
    public void deleteLeaveRequest(UUID requestId, UUID actorId) {

        LeaveRequest request = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found"));

        if (!request.getEmployee().getId().equals(actorId)) {
            throw new IllegalArgumentException("You can only delete your own leave request");
        }

        if (request.getStatus() != LeaveStatus.DRAFT &&
                request.getStatus() != LeaveStatus.CANCELLED) {
            throw new IllegalStateException("Only DRAFT or CANCELLED requests can be deleted");
        }

        // delete from Cloudinary — collect failures instead of failing fast
        List<String> failedDeletes = new ArrayList<>();
        request.getAttachments().forEach(att -> {
            try {
                fileStorageService.delete(att.getPublicId());
            } catch (Exception e) {
                failedDeletes.add(att.getPublicId());
            }
        });

        if (!failedDeletes.isEmpty()) {
            throw new IllegalStateException(
                    "Failed to delete attachments from storage: " + failedDeletes +
                            ". Leave request not deleted."
            );
        }

        leaveRequestRepository.delete(request);
    }

    private void validateStatus(LeaveRequest request) {

        if (request.getStatus() != LeaveStatus.SUBMITTED) {
            throw new IllegalStateException("Only SUBMITTED requests can be approved");
        }
    }




    private boolean isEligible(User employee, LeaveType leaveType) {
        return switch (leaveType.getEligibility()) {
            case ALL -> true;
            case MALE_ONLY -> employee.getGender() == Gender.MALE;
            case FEMALE_ONLY -> employee.getGender() == Gender.FEMALE;
        };
    }

    private void validateLeaveDates(LocalDate startDate, LocalDate endDate) {

        if (startDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Start date cannot be in the past");
        }

        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }
    }

    private LeaveRequestDto.Output mapToOutput(LeaveRequest entity) {

        List<LeaveAttachmentDto.Output> attachments =
                entity.getAttachments()
                        .stream()
                        .map(this::mapAttachmentToOutput)
                        .toList();

        return new LeaveRequestDto.Output(
                entity.getId(),
                UserMapper.mapToLinkedUser(entity.getEmployee()),
                new LeaveTypeDto.Output(
                        entity.getLeaveType().getId(),
                        entity.getLeaveType().getName(),
                        entity.getLeaveType().getDescription(),
                        entity.getLeaveType().getMaxDaysPerYear(),
                        entity.getLeaveType().getRequiresAttachment(),
                        entity.getLeaveType().getIsPaid(),
                        entity.getLeaveType().getEligibility(),
                        entity.getLeaveType().getAllowCarryForward(),
                        entity.getLeaveType().getMaxCarryForwardDays(),
                        entity.getLeaveType().getCreatedAt(),
                        entity.getLeaveType().getUpdatedAt()
                ),

                entity.getStartDate(),
                entity.getEndDate(),
                entity.getDaysRequested(),
                entity.getReason(),
                entity.getStatus(),
                UserMapper.mapToLinkedUser(entity.getDecisionBy()),

                entity.getDecisionAt(),
                entity.getDecisionComment(),
                attachments,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private LeaveAttachmentDto.Output mapAttachmentToOutput(LeaveAttachment attachment) {
        return new LeaveAttachmentDto.Output(
                attachment.getId(),
                attachment.getFileUrl(),
                attachment.getOriginalFileName(),
                attachment.getContentType(),
                attachment.getPublicId(),
                attachment.getFileSize(),
                attachment.getCreatedAt()
        );
    }
}
