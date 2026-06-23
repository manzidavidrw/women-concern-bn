package com.womenconcern.api.project.service;

import com.womenconcern.api.project.dto.request.CreateObjectiveRequest;
import com.womenconcern.api.project.dto.response.ObjectiveResponse;
import java.util.List;
import java.util.UUID;

public interface ObjectiveService {
    ObjectiveResponse createObjective(UUID projectId, CreateObjectiveRequest request);

    ObjectiveResponse getObjectiveById(UUID objectiveId);

    List<ObjectiveResponse> getObjectivesByProject(UUID projectId);

    ObjectiveResponse updateObjective(UUID objectiveId, CreateObjectiveRequest request);

    void deleteObjective(UUID objectiveId);
}