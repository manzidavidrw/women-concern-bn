package com.womenconcern.api.project.service;

import com.womenconcern.api.project.dto.request.CreateOutcomeRequest;
import com.womenconcern.api.project.dto.request.UpdateOutcomeRequest;
import com.womenconcern.api.project.dto.response.OutcomeResponse;

import java.util.List;
import java.util.UUID;

public interface OutcomeService {
    List<OutcomeResponse> createOutcomes(UUID goalId, CreateOutcomeRequest request);
    OutcomeResponse getOutcomeById(UUID outcomeId);
    List<OutcomeResponse> getOutcomesByGoal(UUID goalId);
    OutcomeResponse updateOutcome(UUID outcomeId, UpdateOutcomeRequest request);
    void deleteOutcome(UUID outcomeId);
}