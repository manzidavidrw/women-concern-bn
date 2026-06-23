package com.womenconcern.api.project.service;


import com.womenconcern.api.project.dto.request.CreateActivityRequest;
import com.womenconcern.api.project.dto.response.ActivityResponse;
import java.util.List;
import java.util.UUID;

public interface ActivityService {
    ActivityResponse createActivity(UUID resultId, CreateActivityRequest request);

    ActivityResponse getActivityById(UUID activityId);

    List<ActivityResponse> getActivitiesByResult(UUID resultId);

    ActivityResponse updateActivity(UUID activityId, CreateActivityRequest request);

    void deleteActivity(UUID activityId);

    ActivityResponse assignFieldOfficer(UUID activityId, UUID fieldOfficerId);

    List<ActivityResponse> getActivitiesByFieldOfficer(UUID fieldOfficerId);
}