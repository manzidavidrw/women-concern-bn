package com.womenconcern.api.project.service.impl;


import com.womenconcern.api.exception.ResourceNotFoundException;
import com.womenconcern.api.exception.UnauthorizedException;
import com.womenconcern.api.project.dto.request.CreateProjectRequest;
import com.womenconcern.api.project.dto.response.ProjectResponse;
import com.womenconcern.api.project.entity.Project;
import com.womenconcern.api.project.enums.*;
import com.womenconcern.api.project.repository.ProjectRepository;
import com.womenconcern.api.project.service.ProjectService;

import com.womenconcern.api.utils.ProjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;

    @Override
    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request, UUID projectManagerId) {
        log.info("Creating project: {}", request.getName());

        // Check if project name already exists
        projectRepository.findByNameIgnoreCase(request.getName())
                .ifPresent(p -> {
                    throw new IllegalArgumentException("Project with name '" + request.getName() + "' already exists");
                });

        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .projectManagerId(projectManagerId)
                .approvedBudget(request.getTotalBudget())  // ← cap, never changes
                .totalBudget(BigDecimal.ZERO)              // ← starts at 0, grows with activities
                .status(ProjectStatus.DRAFT)
                .projectManagerApprovalStatus(ApprovalStatus.PENDING)
                .financeApprovalStatus(ApprovalStatus.PENDING)
                .executiveApprovalStatus(ApprovalStatus.PENDING)
                .build();

        Project savedProject = projectRepository.save(project);
        log.info("Project created successfully with ID: {}", savedProject.getId());

        return projectMapper.toResponse(savedProject);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(UUID projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + projectId));

        return projectMapper.toResponse(project);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponse> getAllProjects() {
        List<Project> projects = projectRepository.findAll();
        return projects.stream()
                .map(projectMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponse> getProjectsByProjectManager(UUID projectManagerId) {
        List<Project> projects = projectRepository.findByProjectManagerId(projectManagerId);
        return projects.stream()
                .map(projectMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public ProjectResponse updateProject(UUID projectId, CreateProjectRequest request, UUID projectManagerId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + projectId));

        if (!project.getProjectManagerId().equals(projectManagerId)) {
            throw new UnauthorizedException("Only the project manager can update this project");
        }

        if (project.getStatus() != ProjectStatus.DRAFT) {
            throw new IllegalStateException("Project can only be updated in DRAFT status");
        }

        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setTotalBudget(request.getTotalBudget());

        Project updatedProject = projectRepository.save(project);
        return projectMapper.toResponse(updatedProject);
    }

    @Override
    @Transactional
    public void deleteProject(UUID projectId, UUID projectManagerId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + projectId));

        if (!project.getProjectManagerId().equals(projectManagerId)) {
            throw new UnauthorizedException("Only the project manager can delete this project");
        }

        if (project.getStatus() != ProjectStatus.DRAFT) {
            throw new IllegalStateException("Project can only be deleted in DRAFT status");
        }

        projectRepository.delete(project);
        log.info("Project deleted: {}", projectId);
    }


    @Override
    @Transactional
    public ProjectResponse submitProjectForFinanceReview(UUID projectId, UUID projectManagerId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + projectId));

        if (!project.getProjectManagerId().equals(projectManagerId)) {
            throw new UnauthorizedException("Only the project manager can submit this project");
        }

        project.setStatus(ProjectStatus.SUBMITTED_FOR_FINANCE_REVIEW);
        Project updatedProject = projectRepository.save(project);

        log.info("Project submitted for finance review: {}", projectId);
        return projectMapper.toResponse(updatedProject);
    }

    @Override
    @Transactional
    public ProjectResponse approveProjectByFinance(UUID projectId, String approvalNotes, UUID financeTeamMemberId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + projectId));

        if (project.getStatus() != ProjectStatus.SUBMITTED_FOR_FINANCE_REVIEW) {
            throw new IllegalStateException("Project must be in SUBMITTED_FOR_FINANCE_REVIEW status");
        }

        project.setStatus(ProjectStatus.FINANCE_APPROVED);
        project.setFinanceApprovalStatus(ApprovalStatus.APPROVED);
        project.setFinanceApprovalNotes(approvalNotes);
        project.setFinanceApprovedBy(financeTeamMemberId);
        project.setFinanceApprovedAt(Instant.now());

        Project updatedProject = projectRepository.save(project);
        log.info("Project approved by finance team: {}", projectId);

        return projectMapper.toResponse(updatedProject);
    }

    @Override
    @Transactional
    public ProjectResponse rejectProjectByFinance(UUID projectId, String rejectionReason, UUID financeTeamMemberId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + projectId));

        if (project.getStatus() != ProjectStatus.SUBMITTED_FOR_FINANCE_REVIEW) {
            throw new IllegalStateException("Project must be in SUBMITTED_FOR_FINANCE_REVIEW status");
        }

        project.setStatus(ProjectStatus.FINANCE_REJECTED);
        project.setFinanceApprovalStatus(ApprovalStatus.REJECTED);
        project.setFinanceApprovalNotes(rejectionReason);
        project.setFinanceApprovedBy(financeTeamMemberId);
        project.setFinanceApprovedAt(Instant.now());

        Project updatedProject = projectRepository.save(project);
        log.info("Project rejected by finance team: {}", projectId);

        return projectMapper.toResponse(updatedProject);
    }

    @Override
    @Transactional
    public ProjectResponse submitProjectForExecutiveReview(UUID projectId, UUID financeTeamMemberId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + projectId));

        if (project.getFinanceApprovalStatus() != ApprovalStatus.APPROVED) {
            throw new IllegalStateException("Finance team must approve the project first");
        }

        project.setStatus(ProjectStatus.SUBMITTED_FOR_EXECUTIVE_REVIEW);
        Project updatedProject = projectRepository.save(project);

        log.info("Project submitted for executive review: {}", projectId);
        return projectMapper.toResponse(updatedProject);
    }

    @Override
    @Transactional
    public ProjectResponse approveProjectByExecutive(UUID projectId, String approvalNotes, UUID executiveDirectorId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + projectId));

        if (project.getStatus() != ProjectStatus.SUBMITTED_FOR_EXECUTIVE_REVIEW) {
            throw new IllegalStateException("Project must be in SUBMITTED_FOR_EXECUTIVE_REVIEW status");
        }

        project.setStatus(ProjectStatus.EXECUTIVE_APPROVED);
        project.setExecutiveApprovalStatus(ApprovalStatus.APPROVED);
        project.setExecutiveApprovalNotes(approvalNotes);
        project.setExecutiveApprovedBy(executiveDirectorId);
        project.setExecutiveApprovedAt(Instant.now());

        Project updatedProject = projectRepository.save(project);
        log.info("Project approved by executive director: {}", projectId);

        return projectMapper.toResponse(updatedProject);
    }

    @Override
    @Transactional
    public ProjectResponse rejectProjectByExecutive(UUID projectId, String rejectionReason, UUID executiveDirectorId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + projectId));

        if (project.getStatus() != ProjectStatus.SUBMITTED_FOR_EXECUTIVE_REVIEW) {
            throw new IllegalStateException("Project must be in SUBMITTED_FOR_EXECUTIVE_REVIEW status");
        }

        project.setStatus(ProjectStatus.EXECUTIVE_REJECTED);
        project.setExecutiveApprovalStatus(ApprovalStatus.REJECTED);
        project.setExecutiveApprovalNotes(rejectionReason);
        project.setExecutiveApprovedBy(executiveDirectorId);
        project.setExecutiveApprovedAt(Instant.now());

        Project updatedProject = projectRepository.save(project);
        log.info("Project rejected by executive director: {}", projectId);

        return projectMapper.toResponse(updatedProject);
    }

    @Override
    @Transactional
    public ProjectResponse startImplementation(UUID projectId, UUID executiveDirectorId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + projectId));

        if (project.getExecutiveApprovalStatus() != ApprovalStatus.APPROVED) {
            throw new IllegalStateException("Executive director must approve the project first");
        }

        project.setStatus(ProjectStatus.IMPLEMENTATION);
        Project updatedProject = projectRepository.save(project);

        log.info("Project moved to implementation phase: {}", projectId);
        return projectMapper.toResponse(updatedProject);
    }
}
