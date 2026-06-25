package com.womenconcern.api.project.service.impl;

import com.womenconcern.api.exception.ResourceNotFoundException;
import com.womenconcern.api.project.dto.request.CreateResultRequest;
import com.womenconcern.api.project.dto.response.ResultResponse;
import com.womenconcern.api.project.entity.Outcome;
import com.womenconcern.api.project.entity.Result;
import com.womenconcern.api.project.repository.OutcomeRepository;
import com.womenconcern.api.project.repository.ResultRepository;
import com.womenconcern.api.project.service.BudgetService;
import com.womenconcern.api.project.service.ResultService;
import com.womenconcern.api.utils.ResultMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResultServiceImpl implements ResultService {

    private final ResultRepository resultRepository;
    private final OutcomeRepository outcomeRepository;
    private final ResultMapper resultMapper;
    private final BudgetService budgetService;

    @Override
    @Transactional
    public ResultResponse createResult(UUID outcomeId, CreateResultRequest request) {
        log.info("Creating result for outcome: {}", outcomeId);

        Outcome outcome = outcomeRepository.findById(outcomeId)
                .orElseThrow(() -> new ResourceNotFoundException("Outcome not found with ID: " + outcomeId));

        Result result = Result.builder()
                .outcome(outcome)
                .title(request.getTitle())
                .description(request.getDescription())
                .build();

        Result savedResult = resultRepository.save(result);

        // Recalculate project budget
        budgetService.recalculateBudgetForProject(outcome.getImpact().getObjective().getProject().getId());

        log.info("Result created successfully with ID: {}", savedResult.getId());
        return resultMapper.toResponse(savedResult);
    }

    @Override
    @Transactional(readOnly = true)
    public ResultResponse getResultById(UUID resultId) {
        Result result = resultRepository.findById(resultId)
                .orElseThrow(() -> new ResourceNotFoundException("Result not found with ID: " + resultId));

        return resultMapper.toResponse(result);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResultResponse> getResultsByOutcome(UUID outcomeId) {
        outcomeRepository.findById(outcomeId)
                .orElseThrow(() -> new ResourceNotFoundException("Outcome not found with ID: " + outcomeId));

        List<Result> results = resultRepository.findByOutcomeId(outcomeId);
        return results.stream()
                .map(resultMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public ResultResponse updateResult(UUID resultId, CreateResultRequest request) {
        Result result = resultRepository.findById(resultId)
                .orElseThrow(() -> new ResourceNotFoundException("Result not found with ID: " + resultId));

        result.setTitle(request.getTitle());
        result.setDescription(request.getDescription());

        Result updatedResult = resultRepository.save(result);

        // Recalculate project budget
        budgetService.recalculateBudgetForProject(result.getOutcome().getImpact().getObjective().getProject().getId());

        log.info("Result updated: {}", resultId);
        return resultMapper.toResponse(updatedResult);
    }

    @Override
    @Transactional
    public void deleteResult(UUID resultId) {
        Result result = resultRepository.findById(resultId)
                .orElseThrow(() -> new ResourceNotFoundException("Result not found with ID: " + resultId));

        UUID projectId = result.getOutcome().getImpact().getObjective().getProject().getId();

        resultRepository.delete(result);

        // Recalculate project budget
        budgetService.recalculateBudgetForProject(projectId);

        log.info("Result deleted: {}", resultId);
    }
}
