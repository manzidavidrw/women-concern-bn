package com.womenconcern.api.project.service;

import com.womenconcern.api.project.dto.request.CreateResultRequest;
import com.womenconcern.api.project.dto.response.ResultResponse;
import java.util.List;
import java.util.UUID;

public interface ResultService {
    ResultResponse createResult(UUID outcomeId, CreateResultRequest request);

    ResultResponse getResultById(UUID resultId);

    List<ResultResponse> getResultsByOutcome(UUID outcomeId);

    ResultResponse updateResult(UUID resultId, CreateResultRequest request);

    void deleteResult(UUID resultId);
}
