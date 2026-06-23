package com.womenconcern.api.project.service.impl;


import com.womenconcern.api.exception.ResourceNotFoundException;
import com.womenconcern.api.project.dto.request.CreateActivityRequest;
import com.womenconcern.api.project.dto.response.ActivityResponse;
import com.womenconcern.api.project.entity.Activity;
import com.womenconcern.api.project.entity.Result;
import com.womenconcern.api.project.repository.ActivityRepository;
import com.womenconcern.api.project.repository.ResultRepository;
import com.womenconcern.api.project.service.ActivityService;
import com.womenconcern.api.project.service.BudgetService;
import com.womenconcern.api.utils.ActivityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityServiceImpl implements ActivityService {

    private final ActivityRepository activityRepository;
    private final ResultRepository resultRepository;
    private final ActivityMapper activityMapper;
    private final BudgetService budgetService;

    @Override
    @Transactional
    public ActivityResponse createActivity(UUID resultId, CreateActivityRequest request) {
        log.info("Creating activity for result: {}", resultId);

        Result result = resultRepository.findById(resultId)
                .orElseThrow(() -> new ResourceNotFoundException("Result not found with ID: " + resultId));

        Activity activity = Activity.builder()
                .result(result)
                .title(request.getTitle())
                .description(request.getDescription())
                .costEstimate(request.getCostEstimate())
                .build();

        Activity savedActivity = activityRepository.save(activity);

        // Update activity budget
        savedActivity.updateTotalBudget();
        activityRepository.save(savedActivity);

        // Recalculate project budget
        budgetService.recalculateBudgetForActivity(savedActivity);

        log.info("Activity created successfully with ID: {}", savedActivity.getId());
        return activityMapper.toResponse(savedActivity);
    }

    @Override
    @Transactional(readOnly = true)
    public ActivityResponse getActivityById(UUID activityId) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found with ID: " + activityId));

        return activityMapper.toResponse(activity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActivityResponse> getActivitiesByResult(UUID resultId) {
        resultRepository.findById(resultId)
                .orElseThrow(() -> new ResourceNotFoundException("Result not found with ID: " + resultId));

        List<Activity> activities = activityRepository.findByResultId(resultId);
        return activities.stream()
                .map(activityMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public ActivityResponse updateActivity(UUID activityId, CreateActivityRequest request) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found with ID: " + activityId));

        activity.setTitle(request.getTitle());
        activity.setDescription(request.getDescription());
        activity.setCostEstimate(request.getCostEstimate());

        Activity updatedActivity = activityRepository.save(activity);

        // Update activity budget
        updatedActivity.updateTotalBudget();
        activityRepository.save(updatedActivity);

        // Recalculate project budget
        budgetService.recalculateBudgetForActivity(updatedActivity);

        log.info("Activity updated: {}", activityId);
        return activityMapper.toResponse(updatedActivity);
    }

    @Override
    @Transactional
    public void deleteActivity(UUID activityId) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found with ID: " + activityId));

        Result result = activity.getResult();

        activityRepository.delete(activity);

        // Recalculate project budget
        budgetService.recalculateBudgetForActivity(activity);

        log.info("Activity deleted: {}", activityId);
    }

    @Override
    @Transactional
    public ActivityResponse assignFieldOfficer(UUID activityId, UUID fieldOfficerId) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found with ID: " + activityId));

        activity.setFieldOfficerId(fieldOfficerId);

        Activity updatedActivity = activityRepository.save(activity);

        log.info("Field officer {} assigned to activity {}", fieldOfficerId, activityId);
        return activityMapper.toResponse(updatedActivity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActivityResponse> getActivitiesByFieldOfficer(UUID fieldOfficerId) {
        List<Activity> activities = activityRepository.findByFieldOfficerId(fieldOfficerId);
        return activities.stream()
                .map(activityMapper::toResponse)
                .toList();
    }
}