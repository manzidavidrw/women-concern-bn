package com.womenconcern.api.project.service;


import com.womenconcern.api.project.dto.request.CreateOutcomeRequest;
import com.womenconcern.api.project.dto.response.OutcomeResponse;
import java.util.List;
import java.util.UUID;

public interface OutcomeService {
    OutcomeResponse createOutcome(UUID impactId, CreateOutcomeRequest request);

    OutcomeResponse getOutcomeById(UUID outcomeId);

    List<OutcomeResponse> getOutcomesByImpact(UUID impactId);

    OutcomeResponse updateOutcome(UUID outcomeId, CreateOutcomeRequest request);

    void deleteOutcome(UUID outcomeId);
}