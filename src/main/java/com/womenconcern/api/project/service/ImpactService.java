package com.womenconcern.api.project.service;

import com.womenconcern.api.project.dto.request.CreateImpactRequest;
import com.womenconcern.api.project.dto.response.ImpactResponse;
import java.util.List;
import java.util.UUID;

public interface ImpactService {
    ImpactResponse createImpact(UUID objectiveId, CreateImpactRequest request);

    ImpactResponse getImpactById(UUID impactId);

    List<ImpactResponse> getImpactsByObjective(UUID objectiveId);

    ImpactResponse updateImpact(UUID impactId, CreateImpactRequest request);

    void deleteImpact(UUID impactId);
}