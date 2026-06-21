package com.womenconcern.api.project.service;

import com.womenconcern.api.project.dto.request.CreateActivityRequest;
import com.womenconcern.api.project.dto.request.UpdateActivityRequest;
import com.womenconcern.api.project.dto.response.ActivityResponse;
import com.womenconcern.api.project.entity.Activity;
import com.womenconcern.api.project.entity.Task;

import java.util.List;
import java.util.UUID;

public interface ActivityService {
    List<ActivityResponse> createActivities(UUID resultId, CreateActivityRequest request);
    ActivityResponse getActivityById(UUID activityId);
    List<ActivityResponse> getActivitiesByResult(UUID resultId);
    ActivityResponse updateActivity(UUID activityId, UpdateActivityRequest request);
    void deleteActivity(UUID activityId);
    ActivityResponse assignFieldOfficer(UUID activityId, UUID fieldOfficerId);
    ActivityResponse unassignFieldOfficer(UUID activityId);
    List<ActivityResponse> getActivitiesByFieldOfficer(UUID fieldOfficerId);

}