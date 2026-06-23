package com.womenconcern.api.leave.service.impl;

import com.womenconcern.api.leave.dto.LeaveTypeDto;
import com.womenconcern.api.leave.entity.LeaveType;
import com.womenconcern.api.leave.repository.LeaveTypeRepository;
import com.womenconcern.api.leave.service.ILeaveTypeService;
import com.womenconcern.api.utils.PageResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class LeaveTypeServiceImpl implements ILeaveTypeService {

    private final LeaveTypeRepository leaveTypeRepository;

    @Override
    public LeaveTypeDto.Output createLeaveType(LeaveTypeDto.Input input) {

        if (leaveTypeRepository.existsByName(input.name())) {
            throw new IllegalArgumentException("Leave type already exists: " + input.name());
        }

        LeaveType entity = new LeaveType();
        entity.setName(input.name());
        entity.setDescription(input.description());
        entity.setMaxDaysPerYear(input.maxDaysPerYear());
        entity.setRequiresAttachment(input.requiresAttachment());
        entity.setIsPaid(input.isPaid());

        LeaveType saved = leaveTypeRepository.save(entity);
        return mapToOutput(saved);
    }

    @Override
    public LeaveTypeDto.Output updateLeaveType(UUID id, LeaveTypeDto.Input input) {
        LeaveType entity = leaveTypeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Leave type not found: " + id));

        // check duplicate name (ignore same record)
        leaveTypeRepository.findByName(input.name()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new IllegalArgumentException("Leave type already exists: " + input.name());
            }
        });

        entity.setName(input.name());
        entity.setDescription(input.description());
        entity.setMaxDaysPerYear(input.maxDaysPerYear());
        entity.setRequiresAttachment(input.requiresAttachment());
        entity.setIsPaid(input.isPaid());

        return mapToOutput(entity);
    }

    @Override
    public LeaveTypeDto.Output getLeaveTypeById(UUID id) {
        LeaveType entity = leaveTypeRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Leave type not found"));
        return mapToOutput(entity);
    }

    @Override
    public PageResponse<LeaveTypeDto.Output> getAllLeaveTypes(Pageable pageable) {

        Page<LeaveTypeDto.Output> pageResult =  leaveTypeRepository.findAll(pageable)
                .map(this::mapToOutput);
        return new PageResponse<>(
                pageResult.getContent(),
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                pageResult.isFirst(),
                pageResult.isLast()
        );
    }

    @Override
    public void deleteLeaveType(UUID id) {
        if(!leaveTypeRepository.existsById(id)) {
            throw new EntityNotFoundException("Leave type not found");
        }
        leaveTypeRepository.deleteById(id);
    }

    private LeaveTypeDto.Output mapToOutput(LeaveType entity) {
        return new LeaveTypeDto.Output(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getMaxDaysPerYear(),
                entity.getRequiresAttachment(),
                entity.getIsPaid(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

}
