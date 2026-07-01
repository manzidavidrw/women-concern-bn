package com.womenconcern.api.project.service.impl;

import com.womenconcern.api.auth.entity.User;
import com.womenconcern.api.auth.enums.UserRole;
import com.womenconcern.api.auth.repository.UserRepository;
import com.womenconcern.api.auth.service.impl.EmailService;
import com.womenconcern.api.exception.ResourceNotFoundException;
import com.womenconcern.api.project.dto.request.CreateActivityRequest;
import com.womenconcern.api.project.dto.request.UpdateActivityRequest;
import com.womenconcern.api.project.dto.response.ActivityResponse;
import com.womenconcern.api.project.entity.Activity;
import com.womenconcern.api.project.entity.Result;
import com.womenconcern.api.project.enums.ProjectStatus;
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
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Override
    @Transactional
    public List<ActivityResponse> createActivities(UUID resultId, CreateActivityRequest request) {
        log.info("Creating {} activities for result: {}", request.getActivities().size(), resultId);

        Result result = resultRepository.findById(resultId)
                .orElseThrow(() -> new ResourceNotFoundException("Result not found with ID: " + resultId));

        // Build without saving first so we can validate each one
        List<Activity> activities = request.getActivities().stream()
                .map(item -> Activity.builder()
                        .result(result)
                        .title(item.getTitle())
                        .description(item.getDescription())
                        .costEstimate(item.getCostEstimate())
                        .build())
                .toList();

        // Validate BEFORE saving — throws if any activity exceeds remaining budget
        budgetService.validateActivityBatch(activities);
        List<Activity> savedActivities = activityRepository.saveAll(activities);

        savedActivities.forEach(a -> {
            a.updateTotalBudget();
            activityRepository.save(a);
        });

        budgetService.recalculateBudgetForActivity(savedActivities.get(0));

        log.info("{} activities created successfully for result: {}", savedActivities.size(), resultId);
        return savedActivities.stream()
                .map(activityMapper::toResponse)
                .toList();
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

        return activityRepository.findByResultId(resultId).stream()
                .map(activityMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public ActivityResponse updateActivity(UUID activityId, UpdateActivityRequest request) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found with ID: " + activityId));
        validateProjectIsInImplementation(activity);

        activity.setTitle(request.getTitle());
        activity.setDescription(request.getDescription());
        activity.setCostEstimate(request.getCostEstimate());

        // Validate BEFORE saving — the activity already has an ID so
        // validateActivityBudget excludes it from the sum automatically
        budgetService.validateActivityBudget(activity);

        Activity updatedActivity = activityRepository.save(activity);
        updatedActivity.updateTotalBudget();
        activityRepository.save(updatedActivity);

        budgetService.recalculateBudgetForActivity(updatedActivity);

        log.info("Activity updated: {}", activityId);
        return activityMapper.toResponse(updatedActivity);
    }

    @Override
    @Transactional
    public void deleteActivity(UUID activityId) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found with ID: " + activityId));
        validateProjectIsInImplementation(activity);


        activityRepository.delete(activity);

        // Recalculate from result upward after deletion
        budgetService.recalculateBudgetForProject(
                activity.getResult().getOutcome().getGoal().getProject().getId()
        );

        log.info("Activity deleted: {}", activityId);
    }

    @Override
    @Transactional
    public ActivityResponse assignFieldOfficer(UUID activityId, UUID fieldOfficerId) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found with ID: " + activityId));

        validateProjectIsInImplementation(activity);

        User fieldOfficer = userRepository.findById(fieldOfficerId)
                .orElseThrow(() -> new ResourceNotFoundException("Field officer not found with ID: " + fieldOfficerId));

        if (fieldOfficer.getRole() != UserRole.FIELD_OFFICER) {
            throw new IllegalArgumentException("User is not a Field Officer");
        }

        activity.setFieldOfficerId(fieldOfficerId);
        Activity updatedActivity = activityRepository.save(activity);

        // ✅ Send email
        emailService.sendActivityAssignmentEmail(
                fieldOfficer.getEmail(),
                fieldOfficer.getFirstName(),
                activity.getTitle()
        );

        log.info("Field officer {} assigned to activity {}", fieldOfficerId, activityId);
        return activityMapper.toResponse(updatedActivity);
    }
    @Override
    @Transactional
    public ActivityResponse unassignFieldOfficer(UUID activityId) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new ResourceNotFoundException("Activity not found with ID: " + activityId));

        validateProjectIsInImplementation(activity);

        if (activity.getFieldOfficerId() == null) {
            throw new IllegalStateException("Activity has no field officer assigned");
        }

        // Fetch field officer before clearing — needed for email
        User fieldOfficer = userRepository.findById(activity.getFieldOfficerId())
                .orElseThrow(() -> new ResourceNotFoundException("Field officer not found with ID: " + activity.getFieldOfficerId()));

        activity.setFieldOfficerId(null);
        Activity updatedActivity = activityRepository.save(activity);

        // Notify field officer they have been unassigned
        emailService.sendActivityUnassignmentEmail(
                fieldOfficer.getEmail(),
                fieldOfficer.getFirstName(),
                activity.getTitle()
        );

        log.info("Field officer {} unassigned from activity {}", fieldOfficer.getId(), activityId);
        return activityMapper.toResponse(updatedActivity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActivityResponse> getActivitiesByFieldOfficer(UUID fieldOfficerId) {
        return activityRepository.findByFieldOfficerId(fieldOfficerId).stream()
                .map(activityMapper::toResponse)
                .toList();
    }
    private void validateProjectIsInImplementation(Activity activity) {
        ProjectStatus status = activity.getResult()
                .getOutcome()
                .getGoal()
                .getProject()
                .getStatus();

        if (status != ProjectStatus.IMPLEMENTATION) {
            throw new IllegalStateException(
                    "Activities can only be managed when the project is in IMPLEMENTATION status. " +
                            "Current status: " + status
            );
        }
    }
}
