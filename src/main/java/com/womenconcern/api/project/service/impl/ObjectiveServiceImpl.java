package com.womenconcern.api.project.service.impl;


import com.womenconcern.api.exception.ResourceNotFoundException;
import com.womenconcern.api.project.dto.request.CreateObjectiveRequest;
import com.womenconcern.api.project.dto.response.ObjectiveResponse;
import com.womenconcern.api.project.entity.Objective;
import com.womenconcern.api.project.entity.Project;
import com.womenconcern.api.project.repository.ObjectiveRepository;
import com.womenconcern.api.project.repository.ProjectRepository;
import com.womenconcern.api.project.service.BudgetService;
import com.womenconcern.api.project.service.ObjectiveService;
import com.womenconcern.api.utils.ObjectiveMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ObjectiveServiceImpl implements ObjectiveService {

    private final ObjectiveRepository objectiveRepository;
    private final ProjectRepository projectRepository;
    private final ObjectiveMapper objectiveMapper;
    private final BudgetService budgetService;

    @Override
    @Transactional
    public ObjectiveResponse createObjective(UUID projectId, CreateObjectiveRequest request) {
        log.info("Creating objective for project: {}", projectId);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + projectId));

        Objective objective = Objective.builder()
                .project(project)
                .title(request.getTitle())
                .description(request.getDescription())
                .build();

        Objective savedObjective = objectiveRepository.save(objective);

        // Recalculate project budget
        budgetService.recalculateBudgetForProject(projectId);

        log.info("Objective created successfully with ID: {}", savedObjective.getId());
        return objectiveMapper.toResponse(savedObjective);
    }

    @Override
    @Transactional(readOnly = true)
    public ObjectiveResponse getObjectiveById(UUID objectiveId) {
        Objective objective = objectiveRepository.findById(objectiveId)
                .orElseThrow(() -> new ResourceNotFoundException("Objective not found with ID: " + objectiveId));

        return objectiveMapper.toResponse(objective);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ObjectiveResponse> getObjectivesByProject(UUID projectId) {
        projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + projectId));

        List<Objective> objectives = objectiveRepository.findByProjectId(projectId);
        return objectives.stream()
                .map(objectiveMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public ObjectiveResponse updateObjective(UUID objectiveId, CreateObjectiveRequest request) {
        Objective objective = objectiveRepository.findById(objectiveId)
                .orElseThrow(() -> new ResourceNotFoundException("Objective not found with ID: " + objectiveId));

        objective.setTitle(request.getTitle());
        objective.setDescription(request.getDescription());

        Objective updatedObjective = objectiveRepository.save(objective);

        // Recalculate project budget
        budgetService.recalculateBudgetForProject(objective.getProject().getId());

        log.info("Objective updated: {}", objectiveId);
        return objectiveMapper.toResponse(updatedObjective);
    }

    @Override
    @Transactional
    public void deleteObjective(UUID objectiveId) {
        Objective objective = objectiveRepository.findById(objectiveId)
                .orElseThrow(() -> new ResourceNotFoundException("Objective not found with ID: " + objectiveId));

        UUID projectId = objective.getProject().getId();

        objectiveRepository.delete(objective);

        // Recalculate project budget
        budgetService.recalculateBudgetForProject(projectId);

        log.info("Objective deleted: {}", objectiveId);
    }


}