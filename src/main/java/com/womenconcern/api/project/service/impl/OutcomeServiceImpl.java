package com.womenconcern.api.project.service.impl;


import com.womenconcern.api.exception.ResourceNotFoundException;
import com.womenconcern.api.project.dto.request.CreateOutcomeRequest;
import com.womenconcern.api.project.dto.response.OutcomeResponse;
import com.womenconcern.api.project.entity.Impact;
import com.womenconcern.api.project.entity.Outcome;
import com.womenconcern.api.project.repository.ImpactRepository;
import com.womenconcern.api.project.repository.OutcomeRepository;
import com.womenconcern.api.project.service.BudgetService;
import com.womenconcern.api.project.service.OutcomeService;
import com.womenconcern.api.utils.OutcomeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutcomeServiceImpl implements OutcomeService {

    private final OutcomeRepository outcomeRepository;
    private final ImpactRepository impactRepository;
    private final OutcomeMapper outcomeMapper;
    private final BudgetService budgetService;

    @Override
    @Transactional
    public OutcomeResponse createOutcome(UUID impactId, CreateOutcomeRequest request) {
        log.info("Creating outcome for impact: {}", impactId);

        Impact impact = impactRepository.findById(impactId)
                .orElseThrow(() -> new ResourceNotFoundException("Impact not found with ID: " + impactId));

        Outcome outcome = Outcome.builder()
                .impact(impact)
                .title(request.getTitle())
                .description(request.getDescription())
                .build();

        Outcome savedOutcome = outcomeRepository.save(outcome);

        // Recalculate project budget
        budgetService.recalculateBudgetForProject(impact.getObjective().getProject().getId());

        log.info("Outcome created successfully with ID: {}", savedOutcome.getId());
        return outcomeMapper.toResponse(savedOutcome);
    }

    @Override
    @Transactional(readOnly = true)
    public OutcomeResponse getOutcomeById(UUID outcomeId) {
        Outcome outcome = outcomeRepository.findById(outcomeId)
                .orElseThrow(() -> new ResourceNotFoundException("Outcome not found with ID: " + outcomeId));

        return outcomeMapper.toResponse(outcome);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OutcomeResponse> getOutcomesByImpact(UUID impactId) {
        impactRepository.findById(impactId)
                .orElseThrow(() -> new ResourceNotFoundException("Impact not found with ID: " + impactId));

        List<Outcome> outcomes = outcomeRepository.findByImpactId(impactId);
        return outcomes.stream()
                .map(outcomeMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public OutcomeResponse updateOutcome(UUID outcomeId, CreateOutcomeRequest request) {
        Outcome outcome = outcomeRepository.findById(outcomeId)
                .orElseThrow(() -> new ResourceNotFoundException("Outcome not found with ID: " + outcomeId));

        outcome.setTitle(request.getTitle());
        outcome.setDescription(request.getDescription());

        Outcome updatedOutcome = outcomeRepository.save(outcome);

        // Recalculate project budget
        budgetService.recalculateBudgetForProject(outcome.getImpact().getObjective().getProject().getId());

        log.info("Outcome updated: {}", outcomeId);
        return outcomeMapper.toResponse(updatedOutcome);
    }

    @Override
    @Transactional
    public void deleteOutcome(UUID outcomeId) {
        Outcome outcome = outcomeRepository.findById(outcomeId)
                .orElseThrow(() -> new ResourceNotFoundException("Outcome not found with ID: " + outcomeId));

        UUID projectId = outcome.getImpact().getObjective().getProject().getId();

        outcomeRepository.delete(outcome);

        // Recalculate project budget
        budgetService.recalculateBudgetForProject(projectId);

        log.info("Outcome deleted: {}", outcomeId);
    }
}
