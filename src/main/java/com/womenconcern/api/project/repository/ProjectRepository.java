package com.womenconcern.api.project.repository;



import com.womenconcern.api.project.entity.Project;
import com.womenconcern.api.project.enums.ProjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<Project, UUID> {
    Optional<Project> findByNameIgnoreCase(String name);

    List<Project> findByProjectManagerId(UUID projectManagerId);

    List<Project> findByStatus(ProjectStatus status);

    @Query("SELECT p FROM Project p WHERE p.status IN ('SUBMITTED_FOR_PROJECT_MANAGER_REVIEW', 'SUBMITTED_FOR_FINANCE_REVIEW', 'SUBMITTED_FOR_EXECUTIVE_REVIEW')")
    List<Project> findAllPendingApprovals();
}
