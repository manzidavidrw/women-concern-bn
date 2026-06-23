package com.womenconcern.api.project.repository;


import com.womenconcern.api.project.entity.Objective;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ObjectiveRepository extends JpaRepository<Objective, UUID> {
    List<Objective> findByProjectId(UUID projectId);
}