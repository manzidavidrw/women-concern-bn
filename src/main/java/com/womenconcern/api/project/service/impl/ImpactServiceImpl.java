package com.womenconcern.api.project.service.impl;

import com.womenconcern.api.exception.ResourceNotFoundException;
import com.womenconcern.api.project.dto.request.CreateImpactRequest;
import com.womenconcern.api.project.dto.response.ImpactResponse;
import com.womenconcern.api.project.entity.Impact;
import com.womenconcern.api.project.entity.Objective;
import com.womenconcern.api.project.repository.ImpactRepository;
import com.womenconcern.api.project.repository.ObjectiveRepository;
import com.womenconcern.api.project.service.BudgetService;
import com.womenconcern.api.project.service.ImpactService;
import com.womenconcern.api.utils.ImpactMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImpactServiceImpl implements ImpactService {

    private final ImpactRepository impactRepository;
    private final ObjectiveRepository objectiveRepository;
    private final ImpactMapper impactMapper;
    private final BudgetService budgetService;

    @Override
    @Transactional
    public ImpactResponse createImpact(UUID objectiveId, CreateImpactRequest request) {
        log.info("Creating impact for objective: {}", objectiveId);

        Objective objective = objectiveRepository.findById(objectiveId)
                .orElseThrow(() -> new ResourceNotFoundException("Objective not found with ID: " + objectiveId));

        Impact impact = Impact.builder()
                .objective(objective)
                .title(request.getTitle())
                .description(request.getDescription())
                .build();

        Impact savedImpact = impactRepository.save(impact);

        // Recalculate project budget
        budgetService.recalculateBudgetForProject(objective.getProject().getId());

        log.info("Impact created successfully with ID: {}", savedImpact.getId());
        return impactMapper.toResponse(savedImpact);
    }

    @Override
    @Transactional(readOnly = true)
    public ImpactResponse getImpactById(UUID impactId) {
        Impact impact = impactRepository.findById(impactId)
                .orElseThrow(() -> new ResourceNotFoundException("Impact not found with ID: " + impactId));

        return impactMapper.toResponse(impact);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ImpactResponse> getImpactsByObjective(UUID objectiveId) {
        objectiveRepository.findById(objectiveId)
                .orElseThrow(() -> new ResourceNotFoundException("Objective not found with ID: " + objectiveId));

        List<Impact> impacts = impactRepository.findByObjectiveId(objectiveId);
        return impacts.stream()
                .map(impactMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public ImpactResponse updateImpact(UUID impactId, CreateImpactRequest request) {
        Impact impact = impactRepository.findById(impactId)
                .orElseThrow(() -> new ResourceNotFoundException("Impact not found with ID: " + impactId));

        impact.setTitle(request.getTitle());
        impact.setDescription(request.getDescription());

        Impact updatedImpact = impactRepository.save(impact);

        // Recalculate project budget
        budgetService.recalculateBudgetForProject(impact.getObjective().getProject().getId());

        log.info("Impact updated: {}", impactId);
        return impactMapper.toResponse(updatedImpact);
    }

    @Override
    @Transactional
    public void deleteImpact(UUID impactId) {
        Impact impact = impactRepository.findById(impactId)
                .orElseThrow(() -> new ResourceNotFoundException("Impact not found with ID: " + impactId));

        UUID projectId = impact.getObjective().getProject().getId();

        impactRepository.delete(impact);

        // Recalculate project budget
        budgetService.recalculateBudgetForProject(projectId);

        log.info("Impact deleted: {}", impactId);
    }
}
